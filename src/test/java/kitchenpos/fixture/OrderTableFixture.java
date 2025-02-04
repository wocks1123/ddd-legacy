package kitchenpos.fixture;

import kitchenpos.domain.OrderTable;

import java.util.UUID;

public class OrderTableFixture {

    public static OrderTable createOrderTable(final String name) {
        final OrderTable orderTable = new OrderTable();
        orderTable.setId(UUID.randomUUID());
        orderTable.setName(name);
        orderTable.setNumberOfGuests(0);
        orderTable.setOccupied(false);
        return orderTable;
    }

    public static OrderTable createOccupiedTable() {
        final OrderTable orderTable = createOrderTable("테이블1");
        orderTable.setOccupied(true);
        return orderTable;
    }

    public static OrderTable createUnOccupiedTable() {
        final OrderTable orderTable = createOrderTable("테이블1");
        orderTable.setOccupied(false);
        return orderTable;
    }

    public static OrderTable createOrderTableRequest(final int numberOfGuests) {
        final OrderTable orderTable = new OrderTable();
        orderTable.setNumberOfGuests(numberOfGuests);
        return orderTable;
    }

}
