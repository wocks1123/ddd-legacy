package kitchenpos.application;

import kitchenpos.domain.MenuRepository;
import kitchenpos.domain.Product;
import kitchenpos.domain.ProductRepository;
import kitchenpos.fixture.ProductFixture;
import kitchenpos.infra.PurgomalumClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private PurgomalumClient purgomalumClient;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MenuRepository menuRepository;

    @Nested
    @DisplayName("상품 등록")
    class RegisterProduct {

        @Test
        @DisplayName("상품을 등록한다.")
        void testRegisterProduct() {
            // given
            final String name = "후라이드";
            final BigDecimal price = BigDecimal.valueOf(16_000);
            final Product request = ProductFixture.createProductRequest(name, price);
            given(productRepository.save(any())).willReturn(request);
            given(purgomalumClient.containsProfanity(anyString())).willReturn(false);

            // when
            final Product result = productService.create(request);

            // then
            assertThat(result).isNotNull();
            assertAll(
                    () -> assertThat(result.getName()).isEqualTo(name),
                    () -> assertThat(result.getPrice()).isEqualByComparingTo(price)
            );
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("상품은 빈 이름을 가질 수 없다.")
        void testEmptyName(final String name) {
            // given
            final BigDecimal price = BigDecimal.valueOf(16_000);
            final Product request = ProductFixture.createProductRequest(name, price);

            // when & then
            assertThatException()
                    .isThrownBy(() -> productService.create(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @DisplayName("상품의 가격은 0원 이상이어야 한다.")
        @ValueSource(ints = {-1000, -1})
        void testPriceLessThanZero(final int price) {
            // given
            final String name = "후라이드";
            final Product request = ProductFixture.createProductRequest(name, BigDecimal.valueOf(price));

            // when & then
            assertThatException()
                    .isThrownBy(() -> productService.create(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("상품 등록 시 이름의 유해성 여부를 검사한다.")
        void testInappropriateName() {
            // given
            final String name = "부적절한 이름";
            final BigDecimal price = BigDecimal.valueOf(16_000);
            final Product request = ProductFixture.createProductRequest(name, price);
            given(purgomalumClient.containsProfanity(anyString())).willReturn(true);

            // when & then
            assertThatException()
                    .isThrownBy(() -> productService.create(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("상품 가격 변경")
    class ChangeProductPrice {

        @Test
        @DisplayName("지정한 상품의 가격을 변경할 수 있다.")
        void changeProductPriceSuccess() {
            // given
            final String name = "후라이드";
            final BigDecimal price = BigDecimal.valueOf(16_000);
            final BigDecimal newPrice = BigDecimal.valueOf(20_000);
            final Product product = ProductFixture.createProduct(name, price);
            final Product request = ProductFixture.createProductRequest(newPrice);
            given(productRepository.findById(any())).willReturn(Optional.of(product));
            given(menuRepository.findAllByProductId(any())).willReturn(List.of());

            // when
            final Product result = productService.changePrice(product.getId(), request);

            // then
            assertThat(result).isNotNull();
            assertAll(
                    () -> assertThat(result.getId()).isEqualTo(product.getId()),
                    () -> assertThat(result.getName()).isEqualTo(name),
                    () -> assertThat(result.getPrice()).isEqualByComparingTo(newPrice)
            );
        }

        @ParameterizedTest
        @DisplayName("상품 가격은 0원 이상으로만 가능하다.")
        @ValueSource(ints = {-1000, -1})
        void changeProductPriceFailsWithNegativePrice(final int amount) {
            // given
            final String name = "후라이드";
            final BigDecimal price = BigDecimal.valueOf(16_000);
            final BigDecimal newPrice = BigDecimal.valueOf(amount);
            final Product product = ProductFixture.createProduct(name, price);
            final Product request = ProductFixture.createProductRequest(newPrice);

            // when & then
            assertThatException()
                    .isThrownBy(() -> productService.changePrice(product.getId(), request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("등록되지 않은 상품의 가격을 변경할 수 없다.")
        void testNonExistingProduct() {
            // given
            final UUID nonExistingProductId = UUID.randomUUID();
            final BigDecimal newPrice = BigDecimal.valueOf(20_000);
            final Product request = ProductFixture.createProductRequest(newPrice);
            given(productRepository.findById(any())).willReturn(Optional.empty());

            // when & then
            assertThatException()
                    .isThrownBy(() -> productService.changePrice(nonExistingProductId, request))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("가격 변경 후 메뉴에 포함된 상품들의 가격 총합이 메뉴의 가격보다 크면 숨김 처리한다.")
        void testHideMenuIfPriceIsGreaterThanSum() {
            // given
            final Product product = ProductFixture.createProduct("후라이드", BigDecimal.valueOf(16_000));
            final MenuGroup menuGroup = MenuGroupFixture.createMenuGroup("한마리메뉴");
            final MenuProduct menuProduct = MenuProductFixture.createMenuProduct(product, 1);
            final Menu menu = MenuFixture.createMenu("후라이드치킨", BigDecimal.valueOf(17_000), menuGroup, List.of(menuProduct));
            final BigDecimal newPrice = BigDecimal.valueOf(20_000);
            final Product request = ProductFixture.createProductRequest(newPrice);
            given(productRepository.findById(any())).willReturn(Optional.of(product));
            given(menuRepository.findAllByProductId(any())).willReturn(List.of(menu));

            // when
            final Product result = productService.changePrice(product.getId(), request);

            // then
            assertThat(result).isNotNull();
            assertThat(menu.isDisplayed()).isFalse();
        }

    }

    @Nested
    @DisplayName("상품 조회")
    class FindAllProducts {

        @Test
        @DisplayName("등록된 모든 상품의 목록을 조회한다.")
        void findAllProductsSuccess() {
            // given
            final Product product1 = ProductFixture.createProduct("PRODUCT_1", BigDecimal.valueOf(1000));
            final Product product2 = ProductFixture.createProduct("PRODUCT_2", BigDecimal.valueOf(2000));
            given(productRepository.findAll()).willReturn(List.of(product1, product2));

            // when
            final List<Product> result = productService.findAll();

            // then
            assertThat(result)
                    .hasSize(2)
                    .extracting(Product::getName)
                    .containsExactly("PRODUCT_1", "PRODUCT_2");
        }
    }

}
