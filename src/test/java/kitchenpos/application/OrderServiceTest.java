package kitchenpos.application;

import kitchenpos.domain.*;
import kitchenpos.fixture.MenuFixture;
import kitchenpos.fixture.OrderFixture;
import kitchenpos.fixture.OrderTableFixture;
import kitchenpos.infra.KitchenridersClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderTableRepository orderTableRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private KitchenridersClient kitchenridersClient;

    @Nested
    @DisplayName("주문 등록")
    class RegisterOrder {

        @Test
        @DisplayName("주문을 등록한다.")
        void testRegisterOrder() {
            // given
            // when
            // then
        }

        @ParameterizedTest
        @DisplayName("주문 유형이 배달주문이면 배달주소를 입력해야한다.")
        @NullSource
        void testRegisterOrderWithDeliveryAddress(final String deliveryAddress) {
            // given
            final Order request = OrderFixture.createDeliveryOrderRequest(deliveryAddress);

            // when & then
            assertThatException()
                    .isThrownBy(() -> orderService.create(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("주문 유형이 매장식사인 경우 지정한 주문테이블이 사용 중이어야 한다.")
        void testRegisterOrderWithOrderTable() {
            // given
            final OrderTable orderTable = OrderTableFixture.createUnOccupiedTable();
            final Order request = OrderFixture.createEatInOrderRequest(orderTable);

            // when & then
            assertThatException()
                    .isThrownBy(() -> orderService.create(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("주문 항목을 1개 이상 포함해야 한다.")
        void testRegisterOrderWithOrderLineItems() {
            // given
            final List<OrderLineItem> emptyOrderLineItems = List.of();
            final Order request = OrderFixture.createOrderRequest(OrderType.TAKEOUT, emptyOrderLineItems);

            // when & then
            assertThatException()
                    .isThrownBy(() -> orderService.create(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("등록된 메뉴만 주문 항목에 포함될 수 있다.")
        void testRegisterOrderWithRegisteredMenu() {
            // given
            final Order request = OrderFixture.createTakeOutOrderRequest();
            given(menuRepository.findAllByIdIn(any())).willReturn(List.of());

            // when & then
            assertThatException()
                    .isThrownBy(() -> orderService.create(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @EnumSource(value = OrderType.class, names = {"DELIVERY", "TAKEOUT"})
        @DisplayName("주문 유형이 배달이나 포장인 경우 주문한 메뉴의 수량은 반드시 1개 이상이어야 한다.")
        void testRegisterOrderWithOrderLineItemQuantity(final OrderType orderType) {
            // given
            final Menu menu = MenuFixture.createMenu();
            final OrderLineItem orderLineItem = OrderFixture.createOrderLineItem(menu, -1);
            final Order request = OrderFixture.createOrderRequest(orderType, List.of(orderLineItem));
            given(menuRepository.findAllByIdIn(any())).willReturn(List.of(menu));

            // when & then
            assertThatException()
                    .isThrownBy(() -> orderService.create(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("노출 중이 아닌 메뉴는 주문할 수 없다.")
        void testRegisterOrderWithVisibleMenu() {
            // given
            final Menu hiddenMenu = MenuFixture.createHiddenMenu();
            final OrderLineItem orderLineItem = OrderFixture.createOrderLineItem(hiddenMenu);
            final Order request = OrderFixture.createOrderRequest(OrderType.TAKEOUT, List.of(orderLineItem));
            given(menuRepository.findAllByIdIn(any())).willReturn(List.of(hiddenMenu));
            given(menuRepository.findById(any())).willReturn(Optional.of(hiddenMenu));

            // when & then
            assertThatException()
                    .isThrownBy(() -> orderService.create(request))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("주문한 메뉴의 가격은 메뉴의 가격과 일치해야 한다.")
        void testRegisterOrderWithOrderLineItemPrice() {
            // given
            // when
            // then
        }

        @Test
        @DisplayName("생성한 주문은 대기 상태가 된다.")
        void testRegisterOrderWithStatus() {
            // given
            // when
            // then
        }
    }

    @Nested
    @DisplayName("주문 수락")
    class AcceptOrder {

        @Test
        @DisplayName("주문 상태를 수락(ACCEPTED)으로 변경한다.")
        void testAcceptOrder() {
            // given
            // when
            // then
        }

        @Test
        @DisplayName("대기 중인 주문이 아닌 경우 수락할 수 없다.")
        void testAcceptOrderWithStatus() {
            // given
            // when
            // then
        }

        @Test
        @DisplayName("주문을 수락하면 배달 라이더에게 주문을 전달한다.")
        void testAcceptOrderWithDelivery() {
            // given
            // when
            // then
        }
    }

    @Nested
    @DisplayName("주문 제공")
    class ServeOrder {

        @Test
        @DisplayName("주문 상태를 제공(SERVED)으로 변경한다.")
        void testServeOrder() {
            // given
            // when
            // then
        }

        @Test
        @DisplayName("수락(ACCEPTED)된 주문만 변경할 수 있다.")
        void testServeOrderWithStatus() {
            // given
            // when
            // then
        }
    }

    @Nested
    @DisplayName("주문 배달 시작")
    class DeliveryOrder {

        @Test
        @DisplayName("주문 상태를 배달중(DELIVERING)으로 변경한다.")
        void testDeliveryOrder() {
            // given
            // when
            // then
        }

        @Test
        @DisplayName("수락(ACCEPTED)된 주문만 배달할 수 있다.")
        void testDeliveryOrderWithStatus() {
            // given
            // when
            // then
        }
    }

    @Nested
    @DisplayName("주문 배달 완료")
    class CompleteDelivery {

        @Test
        @DisplayName("주문 상태를 배달완료(COMPLETED)으로 변경한다.")
        void testCompleteDelivery() {
            // given
            // when
            // then
        }

        @Test
        @DisplayName("배달중(DELIVERING)인 주문만 배달 완료할 수 있다.")
        void testCompleteDeliveryWithStatus() {
            // given
            // when
            // then
        }
    }

    @Nested
    @DisplayName("주문 완료")
    class CompleteOrder {

        @Test
        @DisplayName("주문 상태를 완료(COMPLETED)로 변경한다.")
        void testCompleteOrder() {
            // given
            // when
            // then
        }

        @Test
        @DisplayName("주문 유형이 배달주문이면 배달완료(DELIVERED) 상태일 경우에만 변경할 수 있다.")
        void testCompleteOrderWithStatus() {
            // given
            // when
            // then
        }

        @Test
        @DisplayName("주문 유형이 매장식사인 경우 제공완료(SERVED) 상태일 경우에만 변경할 수 있다.")
        void testCompleteOrderWithStatusForEatIn() {
            // given
            // when
            // then
        }

        @Test
        @DisplayName("주문 유형이 포장이면 제공완료(SERVED) 상태일 경우에만 변경할 수 있다.")
        void testCompleteOrderWithStatusForTakeOut() {
            // given
            // when
            // then
        }

        @Test
        @DisplayName("주문 유형이 매장식사일 때 주문 테이블을 정리한다.")
        void testCompleteOrderWithOrderTable() {
            // given
            // when
            // then
        }
    }

}
