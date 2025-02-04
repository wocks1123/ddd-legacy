package kitchenpos.fixture;

import kitchenpos.domain.*;

import java.util.List;
import java.util.UUID;

public class OrderFixture {

    public static Order createOrder(final OrderType type, final List<OrderLineItem> orderLineItems, final String deliveryAddress, final OrderTable orderTable) {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setType(type);
        order.setOrderLineItems(orderLineItems);
        order.setDeliveryAddress(deliveryAddress);
        order.setOrderTable(orderTable);
        order.setOrderTableId(orderTable.getId());
        return order;
    }

    public static Order createOrderRequest(final OrderType orderType, final List<OrderLineItem> orderLineItems) {
        Order order = new Order();
        order.setType(orderType);
        order.setOrderLineItems(orderLineItems);
        return order;
    }

    public static Order createOrderRequest(final OrderType orderType, final List<OrderLineItem> orderLineItems, final OrderTable orderTable) {
        Order order = new Order();
        order.setType(orderType);
        order.setOrderLineItems(orderLineItems);
        order.setOrderTable(orderTable);
        return order;
    }

    public static Order createDeliveryOrderRequest(final String deliveryAddress, final List<OrderLineItem> orderLineItems) {
        return createOrderRequest(OrderType.DELIVERY, orderLineItems);
    }

    public static Order createDeliveryOrderRequest(final String deliveryAddress) {
        return createOrderRequest(OrderType.DELIVERY, List.of(createOrderLineItem()));
    }

    public static Order createEatInOrderRequest(final OrderTable orderTable) {
        return createOrderRequest(OrderType.EAT_IN, List.of(createOrderLineItem()), orderTable);
    }

    public static Order createTakeOutOrderRequest() {
        return createOrderRequest(OrderType.TAKEOUT, List.of(createOrderLineItem()));
    }

    public static OrderLineItem createOrderLineItem(final Menu menu, final long quantity) {
        final OrderLineItem orderLineItem = new OrderLineItem();
        orderLineItem.setMenuId(menu.getId());
        orderLineItem.setQuantity(quantity);
        orderLineItem.setMenu(menu);
        orderLineItem.setPrice(menu.getPrice());
        return orderLineItem;
    }

    public static OrderLineItem createOrderLineItem(final Menu menu) {
        return createOrderLineItem(menu, 1L);
    }

    public static OrderLineItem createOrderLineItem() {
        return createOrderLineItem(MenuFixture.createMenu(), 1L);
    }

}
