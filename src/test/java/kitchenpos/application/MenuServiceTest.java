package kitchenpos.application;

import kitchenpos.domain.*;
import kitchenpos.fixture.MenuFixture;
import kitchenpos.fixture.MenuGroupFixture;
import kitchenpos.fixture.MenuProductFixture;
import kitchenpos.fixture.ProductFixture;
import kitchenpos.infra.PurgomalumClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @InjectMocks
    private MenuService menuService;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private MenuGroupRepository menuGroupRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PurgomalumClient purgomalumClient;

    @Nested
    @DisplayName("메뉴 등록")
    class RegisterMenu {

        @Test
        @DisplayName("메뉴를 등록한다.")
        void testRegisterMenu() {
            // given
            final MenuGroup menuGroup = MenuGroupFixture.createMenuGroup("한마리메뉴");
            final Product product = ProductFixture.createProduct("후라이드", BigDecimal.valueOf(16_000));
            final MenuProduct menuProduct = MenuProductFixture.createMenuProduct(product, 1);
            final Menu request = MenuFixture.createMenuRequest("후라이드치킨", BigDecimal.valueOf(16_000), menuGroup, List.of(menuProduct));
            given(menuGroupRepository.findById(any())).willReturn(Optional.of(menuGroup));
            given(productRepository.findAllByIdIn(any())).willReturn(List.of(product));
            given(productRepository.findById(any())).willReturn(Optional.of(product));
            given(purgomalumClient.containsProfanity(anyString())).willReturn(false);
            given(menuRepository.save(any(Menu.class))).willReturn(request);

            // when
            final Menu result = menuService.create(request);

            // then
            assertThat(result).isNotNull();
            assertAll(
                    () -> assertThat(result.getName()).isEqualTo(request.getName()),
                    () -> assertThat(result.getPrice()).isEqualTo(request.getPrice()),
                    () -> assertThat(result.getMenuGroup()).isEqualTo(menuGroup),
                    () -> assertThat(result.getMenuProducts()).hasSize(1)
            );
        }

        @ParameterizedTest
        @DisplayName("메뉴의 가격은 0원 이상이어야 한다.")
        @ValueSource(ints = {-1000, -1})
        void testPriceLessThanZero(final int amount) {
            // given
            final MenuGroup menuGroup = MenuGroupFixture.createMenuGroup("한마리메뉴");
            final Product product = ProductFixture.createProduct("후라이드", BigDecimal.valueOf(16_000));
            final MenuProduct menuProduct = MenuProductFixture.createMenuProduct(product, 1);
            final Menu request = MenuFixture.createMenuRequest("후라이드치킨", BigDecimal.valueOf(amount), menuGroup, List.of(menuProduct));

            // when & then
            assertThatException()
                    .isThrownBy(() -> menuService.create(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("등록된 상품 중 1개 이상을 포함해야 한다.")
        void testEmptyMenuProducts() {
            // given
            final MenuGroup menuGroup = MenuGroupFixture.createMenuGroup("한마리메뉴");
            final Menu request = MenuFixture.createMenuRequest("후라이드치킨", BigDecimal.valueOf(16_000), menuGroup, List.of());
            given(menuGroupRepository.findById(any())).willReturn(Optional.of(menuGroup));

            // when & then
            assertThatException()
                    .isThrownBy(() -> menuService.create(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("메뉴의 가격은 지정한 상품들의 가격 총합보다 작아야 한다.")
        void testPriceLessThanSumOfProductPrices() {
            // given
            final MenuGroup menuGroup = MenuGroupFixture.createMenuGroup("한마리메뉴");
            final Product product = ProductFixture.createProduct("후라이드", BigDecimal.valueOf(16_000));
            final MenuProduct menuProduct = MenuProductFixture.createMenuProduct(product, 1);
            final Menu request = MenuFixture.createMenuRequest("후라이드치킨", product.getPrice().add(BigDecimal.valueOf(1_000)), menuGroup, List.of(menuProduct));
            given(menuGroupRepository.findById(any())).willReturn(Optional.of(menuGroup));
            given(productRepository.findAllByIdIn(any())).willReturn(List.of(product));
            given(productRepository.findById(any())).willReturn(Optional.of(product));

            // when & then
            assertThatException()
                    .isThrownBy(() -> menuService.create(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("부적절한 이름으로 메뉴를 생성할 수 없다.")
        void testInappropriateName() {
            // given
            final MenuGroup menuGroup = MenuGroupFixture.createMenuGroup("한마리메뉴");
            final Product product = ProductFixture.createProduct("후라이드", BigDecimal.valueOf(16_000));
            final MenuProduct menuProduct = MenuProductFixture.createMenuProduct(product, 1);
            final Menu request = MenuFixture.createMenuRequest("부적절한 이름", BigDecimal.valueOf(16_000), menuGroup, List.of(menuProduct));
            given(menuGroupRepository.findById(any())).willReturn(Optional.of(menuGroup));
            given(productRepository.findAllByIdIn(any())).willReturn(List.of(product));
            given(productRepository.findById(any())).willReturn(Optional.of(product));
            given(purgomalumClient.containsProfanity(anyString())).willReturn(true);

            // when & then
            assertThatException()
                    .isThrownBy(() -> menuService.create(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("메뉴 가격 변경")
    class ChangeMenuPrice {

        @Test
        @DisplayName("지정한 메뉴의 가격을 변경할 수 있다.")
        void testChangeMenuPrice() {
            // given
            final MenuGroup menuGroup = MenuGroupFixture.createMenuGroup("한마리메뉴");
            final Product product = ProductFixture.createProduct("후라이드", BigDecimal.valueOf(16_000));
            final MenuProduct menuProduct = MenuProductFixture.createMenuProduct(product, 1);
            final Menu menu = MenuFixture.createMenu("후라이드치킨", BigDecimal.valueOf(16_000), menuGroup, List.of(menuProduct));
            final Menu request = MenuFixture.createMenuRequest(BigDecimal.valueOf(15_000));

            given(menuRepository.findById(any())).willReturn(Optional.of(menu));

            // when
            Menu updatedMenu = menuService.changePrice(menu.getId(), request);

            // then
            assertThat(updatedMenu).isNotNull();
            assertThat(updatedMenu.getPrice()).isEqualByComparingTo(menu.getPrice());
        }

        @ParameterizedTest
        @DisplayName("메뉴의 가격은 0원 이상이어야 한다.")
        @ValueSource(ints = {-1000, -1})
        void testPriceFailsWithInvalidPrice(final int newPrice) {
            // given
            final MenuGroup menuGroup = MenuGroupFixture.createMenuGroup("한마리메뉴");
            final Product product = ProductFixture.createProduct("후라이드", BigDecimal.valueOf(16_000));
            final MenuProduct menuProduct = MenuProductFixture.createMenuProduct(product, 1);
            final Menu menu = MenuFixture.createMenu("후라이드치킨", BigDecimal.valueOf(16_000), menuGroup, List.of(menuProduct));
            final Menu request = MenuFixture.createMenuRequest(BigDecimal.valueOf(newPrice));

            // when & then
            assertThatException()
                    .isThrownBy(() -> menuService.changePrice(menu.getId(), request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("변경된 가격은 메뉴에 포함된 상품들의 총합보다 작아야 한다.")
        void testPriceLessThanSumOfProductPrices() {
            // given
            final MenuGroup menuGroup = MenuGroupFixture.createMenuGroup("한마리메뉴");
            final Product product = ProductFixture.createProduct("후라이드", BigDecimal.valueOf(16_000));
            final MenuProduct menuProduct = MenuProductFixture.createMenuProduct(product, 1);
            final Menu menu = MenuFixture.createMenu("후라이드치킨", BigDecimal.valueOf(16_000), menuGroup, List.of(menuProduct));
            final Menu request = MenuFixture.createMenuRequest(BigDecimal.valueOf(20_000));

            given(menuRepository.findById(any())).willReturn(Optional.of(menu));

            // when & then
            assertThatException()
                    .isThrownBy(() -> menuService.changePrice(menu.getId(), request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("메뉴 노출")
    class DisplayMenu {

        @Test
        @DisplayName("지정한 메뉴를 노출한다.")
        void testDisplayMenu() {
            // given
            final MenuGroup menuGroup = MenuGroupFixture.createMenuGroup("한마리메뉴");
            final Product product = ProductFixture.createProduct("후라이드", BigDecimal.valueOf(16_000));
            final MenuProduct menuProduct = MenuProductFixture.createMenuProduct(product, 1);
            final Menu menu = MenuFixture.createMenu("후라이드치킨", BigDecimal.valueOf(16_000), menuGroup, List.of(menuProduct));
            given(menuRepository.findById(any())).willReturn(Optional.of(menu));

            // when
            Menu displayedMenu = menuService.display(menu.getId());

            // then
            assertThat(displayedMenu).isNotNull();
            assertThat(displayedMenu.isDisplayed()).isTrue();
        }

        @Test
        @DisplayName("메뉴의 가격이 메뉴에 포함된 상품들의 총합보다 작은 경우에만 노출처리할 수 있다.")
        void testDisplayMenuFailsWithPriceLessThanSumOfProductPrices() {
            // given
            final MenuGroup menuGroup = MenuGroupFixture.createMenuGroup("한마리메뉴");
            final Product product = ProductFixture.createProduct("후라이드", BigDecimal.valueOf(16_000));
            final MenuProduct menuProduct = MenuProductFixture.createMenuProduct(product, 1);
            final Menu menu = MenuFixture.createMenu("후라이드치킨", BigDecimal.valueOf(17_000), menuGroup, List.of(menuProduct));
            given(menuRepository.findById(any())).willReturn(Optional.of(menu));

            // when & then
            assertThatException()
                    .isThrownBy(() -> menuService.display(menu.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("메뉴 숨기기")
    class HideMenu {

        @Test
        @DisplayName("지정한 메뉴를 숨김처리한다.")
        void testHideMenu() {
            // given
            final MenuGroup menuGroup = MenuGroupFixture.createMenuGroup("한마리메뉴");
            final Product product = ProductFixture.createProduct("후라이드", BigDecimal.valueOf(16_000));
            final MenuProduct menuProduct = MenuProductFixture.createMenuProduct(product, 1);
            final Menu menu = MenuFixture.createMenu("후라이드치킨", BigDecimal.valueOf(16_000), menuGroup, List.of(menuProduct));
            given(menuRepository.findById(any())).willReturn(Optional.of(menu));

            // when
            Menu hiddenMenu = menuService.hide(menu.getId());

            // then
            assertThat(hiddenMenu).isNotNull();
            assertThat(hiddenMenu.isDisplayed()).isFalse();
        }
    }

    @Nested
    @DisplayName("메뉴 조회")
    class FindAllMenus {

        @Test
        @DisplayName("등록된 모든 메뉴를 조회한다.")
        void testFindAllMenus() {
            // given
            final MenuGroup menuGroup = MenuGroupFixture.createMenuGroup("한마리메뉴");
            final Product product = ProductFixture.createProduct("후라이드", BigDecimal.valueOf(16_000));
            final MenuProduct menuProduct = MenuProductFixture.createMenuProduct(product, 1);
            final Menu menu = MenuFixture.createMenu("후라이드치킨", BigDecimal.valueOf(16_000), menuGroup, List.of(menuProduct));

            given(menuRepository.findAll()).willReturn(List.of(menu));

            // when
            List<Menu> menus = menuService.findAll();

            // then
            assertThat(menus)
                    .hasSize(1)
                    .extracting(Menu::getId)
                    .containsExactly(menu.getId());
        }
    }

}
