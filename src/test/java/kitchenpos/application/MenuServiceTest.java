package kitchenpos.application;

import kitchenpos.domain.*;
import kitchenpos.fixture.MenuFixture;
import kitchenpos.fixture.MenuGroupFixture;
import kitchenpos.fixture.MenuProductFixture;
import kitchenpos.fixture.ProductFixture;
import kitchenpos.infra.PurgomalumClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class MenuServiceTest {

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MenuGroupRepository menuGroupRepository;

    @MockBean
    private PurgomalumClient purgomalumClient;

    @Nested
    @DisplayName("메뉴 등록")
    class RegisterMenu {

        private MenuGroup menuGroup;
        private MenuProduct menuProduct;

        @BeforeEach
        void setup() {
            final Product product = ProductFixture.createProduct("후라이드", BigDecimal.valueOf(16_000));
            productRepository.save(product);
            menuGroup = MenuGroupFixture.createMenuGroup("한마리메뉴");
            menuGroupRepository.save(menuGroup);
            menuProduct = MenuProductFixture.createMenuProduct(product, 1L);
            when(purgomalumClient.containsProfanity(anyString())).thenReturn(false);
        }

        @Test
        @DisplayName("메뉴를 등록한다.")
        void registerMenu() {
            // given
            final String name = "후라이드치킨";
            final BigDecimal price = BigDecimal.valueOf(16_000);
            final Menu request = MenuFixture.createMenu(name, price, List.of(menuProduct), menuGroup);
            when(purgomalumClient.containsProfanity(anyString())).thenReturn(false);

            // when
            final Menu result = menuService.create(request);

            // then
            final Menu found = menuRepository.findById(result.getId()).orElse(null);

            assertThat(found).isNotNull();
            assertAll(
                    () -> assertThat(found.getName()).isEqualTo(name),
                    () -> assertThat(found.getPrice()).isEqualByComparingTo(price)
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("메뉴는 이름과 가격을 필수로 가진다.")
        void nullOrEmptyName(final String name) {
            // given
            final BigDecimal price = BigDecimal.valueOf(16_000);
            final Menu request = MenuFixture.createMenu(name, price, List.of(menuProduct), menuGroup);
            when(purgomalumClient.containsProfanity(anyString())).thenReturn(false);

            // when & then
            assertThatException()
                    .isThrownBy(() -> menuService.create(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @DisplayName("메뉴의 가격은 0원 이상이어야 한다.")
        @ValueSource(ints = {-1000, -1})
        void priceLessThanZero(final int price) {
            // given
            final String name = "후라이드치킨";
            final Menu request = MenuFixture.createMenu(name, BigDecimal.valueOf(price), List.of(menuProduct), menuGroup);
            when(purgomalumClient.containsProfanity(anyString())).thenReturn(false);

            // when & then
            assertThatException()
                    .isThrownBy(() -> menuService.create(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("메뉴 등록 시 이름의 유해성 여부를 검사한다.")
        void inappropriateName() {
            // given
            final String name = "INAPPROPRIATE_NAME";
            final BigDecimal price = BigDecimal.valueOf(16_000);
            final Menu request = MenuFixture.createMenu(name, price, List.of(menuProduct), menuGroup);
            when(purgomalumClient.containsProfanity(anyString())).thenReturn(true);

            // when & then
            assertThatException()
                    .isThrownBy(() -> menuService.create(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("메뉴 가격 변경")
    class ChangeMenuPrice {
        private UUID existingId;
        private UUID nonExistingId = UUID.randomUUID();

        @BeforeEach
        void setup() {
            final Product product = ProductFixture.createProduct("후라이드", BigDecimal.valueOf(16_000));
            productRepository.save(product);
            final MenuGroup menuGroup = MenuGroupFixture.createMenuGroup("한마리메뉴");
            menuGroupRepository.save(menuGroup);
            final MenuProduct menuProduct = MenuProductFixture.createMenuProduct(product, 1L);
            when(purgomalumClient.containsProfanity(anyString())).thenReturn(false);
            final String name = "후라이드치킨";
            final BigDecimal price = BigDecimal.valueOf(16_000);
            final Menu request = MenuFixture.createMenu(name, price, List.of(menuProduct), menuGroup);
            existingId = menuService.create(request).getId();
        }

        @Test
        @DisplayName("지정한 메뉴의 가격을 변경할 수 있다.")
        void changeMenuPriceSuccess() {
            // given
            final Menu request = new Menu();
            final BigDecimal newPrice = BigDecimal.valueOf(20_000);
            request.setPrice(newPrice);

            // when
            final Menu result = menuService.changePrice(existingId, request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getPrice()).isEqualTo(newPrice);
        }

        @ParameterizedTest
        @DisplayName("메뉴 가격 변경 시 가격이 0원 이상이어야 한다.")
        @ValueSource(ints = {-1000, -1})
        void changeMenuPriceFailsWithNegativePrice(final int price) {
            // given
            final Menu request = new Menu();
            final BigDecimal newPrice = BigDecimal.valueOf(price);
            request.setPrice(newPrice);

            // when & then
            assertThatException()
                    .isThrownBy(() -> menuService.changePrice(existingId, request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("등록되지 않은 메뉴의 가격을 변경할 수 없다.")
        void nonExistingMenu() {
            // given
            final Menu request = new Menu();
            final BigDecimal newPrice = BigDecimal.valueOf(20_000);
            request.setPrice(newPrice);

            // when & then
            assertThatException()
                    .isThrownBy(() -> menuService.changePrice(nonExistingId, request))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("메뉴 조회")
    class FindAllMenus {

        @Test
        @DisplayName("등록된 모든 메뉴의 목록을 조회한다.")
        void findAllMenusSuccess() {
            // given
            final Menu menu1 = createMenu("MENU_1", BigDecimal.valueOf(1000));
            final Menu menu2 = createMenu("MENU_2", BigDecimal.valueOf(2000));
            when(purgomalumClient.containsProfanity(anyString())).thenReturn(false);
            menuService.create(menu1);
            menuService.create(menu2);

            // when
            final List<Menu> result = menuService.findAll();

            // then
            assertThat(result)
                    .hasSize(2)
                    .extracting(Menu::getName)
                    .containsExactly("MENU_1", "MENU_2");
        }
    }

}
