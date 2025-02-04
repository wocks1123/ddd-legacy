package kitchenpos.fixture;

import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class MenuFixture {

    public static Menu createMenu(final String name, final BigDecimal price, final MenuGroup menuGroup, final List<MenuProduct> menuProducts) {
        Menu menu = new Menu();
        menu.setId(UUID.randomUUID());
        menu.setName(name);
        menu.setPrice(price);
        menu.setMenuGroup(menuGroup);
        menu.setMenuProducts(menuProducts);
        return menu;
    }

    public static Menu createMenu() {
        final MenuGroup menuGroup = MenuGroupFixture.createMenuGroup("한마리메뉴");
        final MenuProduct menuProduct = MenuProductFixture.createMenuProduct();
        Menu menu = createMenu("후라이드치킨", BigDecimal.valueOf(16_000), menuGroup, List.of(menuProduct));
        return menu;
    }

    public static Menu createDisplayedMenu() {
        Menu menu = createMenu();
        menu.setDisplayed(true);
        return menu;
    }

    public static Menu createHiddenMenu() {
        Menu menu = createMenu();
        menu.setDisplayed(false);
        return menu;
    }

    public static Menu createMenuRequest(final String name, final BigDecimal price, final MenuGroup menuGroup, final List<MenuProduct> menuProducts) {
        Menu menu = new Menu();
        menu.setName(name);
        menu.setPrice(price);
        menu.setMenuGroup(menuGroup);
        menu.setMenuProducts(menuProducts);
        return menu;
    }

    public static Menu createMenuRequest(final BigDecimal price) {
        return createMenuRequest(null, price, null, null);
    }

}
