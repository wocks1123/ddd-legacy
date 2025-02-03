package kitchenpos.application;

import kitchenpos.domain.OrderRepository;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.OrderTableRepository;
import kitchenpos.fixture.OrderTableFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderTableServiceTest {

    @InjectMocks
    private OrderTableService orderTableService;

    @Mock
    private OrderTableRepository orderTableRepository;

    @Mock
    private OrderRepository orderRepository;


    @Nested
    @DisplayName("주문 테이블 등록")
    class RegisterOrderTable {

        @Test
        @DisplayName("주문 테이블을 등록한다.")
        void testRegisterOrderTable() {
            // given
            final OrderTable request = OrderTableFixture.createOrderTable("테이블1");
            given(orderTableRepository.save(any())).willReturn(request);

            // when
            final OrderTable result = orderTableService.create(request);

            // then
            assertThat(result).isNotNull();
            assertAll(
                    () -> assertThat(result.getId()).isNotNull(),
                    () -> assertThat(result.getName()).isEqualTo(request.getName()),
                    () -> assertThat(result.getNumberOfGuests()).isZero(),
                    () -> assertThat(result.isOccupied()).isFalse()
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("주문 테이블은 이름을 필수로 가진다.")
        void testNullOrEmptyName(final String name) {
            // given
            final OrderTable request = OrderTableFixture.createOrderTable(name);

            // when & then
            assertThatException()
                    .isThrownBy(() -> orderTableService.create(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("주문 테이블 착석")
    class SitOrderTable {

        @Test
        @DisplayName("주문 테이블을 사용중으로 변경한다.")
        void testSitOrderTable() {
            // given
            final OrderTable request = OrderTableFixture.createOrderTable("테이블1");
            given(orderTableRepository.findById(any())).willReturn(Optional.of(request));

            // when
            orderTableService.sit(request.getId());

            // then
            assertThat(request.isOccupied()).isTrue();
        }
    }

    @Nested
    @DisplayName("주문 테이블 정리")
    class ClearOrderTable {

        @Test
        @DisplayName("주문 테이블을 정리한다.")
        void testClearOrderTable() {
            // given
            final OrderTable request = OrderTableFixture.createOrderTable("테이블1");
            given(orderTableRepository.findById(any())).willReturn(Optional.of(request));
            given(orderRepository.existsByOrderTableAndStatusNot(any(), any())).willReturn(false);

            // when
            orderTableService.clear(request.getId());

            // then
            assertThat(request.isOccupied()).isFalse();
        }

        @Test
        @DisplayName("주문 테이블의 모든 주문이 완료되지 않으면 정리할 수 없다.")
        void testClearOrderTableWithNotCompletedOrder() {
            // given
            final OrderTable request = OrderTableFixture.createOrderTable("테이블1");
            given(orderTableRepository.findById(any())).willReturn(Optional.of(request));
            given(orderRepository.existsByOrderTableAndStatusNot(any(), any())).willReturn(true);

            // when & then
            assertThatException()
                    .isThrownBy(() -> orderTableService.clear(request.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("주문 테이블 손님 수 변경")
    class ChangeNumberOfGuests {

        @Test
        @DisplayName("주문 테이블의 손님 수를 변경한다.")
        void testChangeNumberOfGuests() {
            // given
            final OrderTable orderTable = OrderTableFixture.createOrderTable("테이블1");
            orderTable.setOccupied(true);
            final OrderTable request = OrderTableFixture.createOrderTableRequest(4);
            given(orderTableRepository.findById(any())).willReturn(Optional.of(orderTable));

            // when
            orderTableService.changeNumberOfGuests(orderTable.getId(), request);

            // then
            assertThat(orderTable.getNumberOfGuests()).isEqualTo(request.getNumberOfGuests());
        }

        @Test
        @DisplayName("주문 테이블의 손님 수는 0명 이상이어야 한다.")
        void testChangeNumberOfGuestsLessThanZero() {
            // given
            final OrderTable orderTable = OrderTableFixture.createOrderTable("테이블1");
            final OrderTable request = OrderTableFixture.createOrderTableRequest(-1);

            // when & then
            assertThatException()
                    .isThrownBy(() -> orderTableService.changeNumberOfGuests(orderTable.getId(), request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("주문 테이블이 사용중이 아니면 손님 수를 변경할 수 없다.")
        void testChangeNumberOfGuestsWhenOccupied() {
            // given
            final OrderTable orderTable = OrderTableFixture.createOrderTable("테이블1");
            orderTable.setOccupied(false);
            final OrderTable request = OrderTableFixture.createOrderTableRequest(4);
            given(orderTableRepository.findById(any())).willReturn(Optional.of(orderTable));

            // when & then
            assertThatException()
                    .isThrownBy(() -> orderTableService.changeNumberOfGuests(orderTable.getId(), request))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("주문 테이블 조회")
    class FindOrderTable {

        @Test
        @DisplayName("모든 주문 테이블 목록을 조회한다.")
        void testFindAllOrderTables() {
            // given
            final OrderTable orderTable1 = OrderTableFixture.createOrderTable("테이블1");
            final OrderTable orderTable2 = OrderTableFixture.createOrderTable("테이블2");
            given(orderTableRepository.findAll()).willReturn(List.of(orderTable1, orderTable2));

            // when
            final List<OrderTable> result = orderTableService.findAll();

            // then
            assertThat(result)
                    .hasSize(2)
                    .extracting(OrderTable::getId)
                    .containsExactly(orderTable1.getId(), orderTable2.getId());
        }
    }

}
