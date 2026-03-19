package ${package}.stock;

import ${package}.base.MainLayout;
import ${package}.organization.RequiresOrganization;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "blue-stock", layout = MainLayout.class)
@PermitAll
@RequiresOrganization("Blue Corp")
public class BlueStockView extends VerticalLayout {

    public BlueStockView() {
        add(new H2(getTranslation("blue-stock.title")));

        Grid<StockItem> grid = new Grid<>();
        grid.addColumn(StockItem::sku).setHeader(getTranslation("blue-stock.col.sku"));
        grid.addColumn(StockItem::name).setHeader(getTranslation("blue-stock.col.product"));
        grid.addColumn(StockItem::quantity).setHeader(getTranslation("blue-stock.col.quantity"));
        grid.addColumn(StockItem::price).setHeader(getTranslation("blue-stock.col.price"));

        grid.setItems(
                new StockItem("BC-001", "Widget A", 150, 29.99),
                new StockItem("BC-002", "Widget B", 75, 49.99),
                new StockItem("BC-003", "Gadget X", 200, 19.99)
        );

        add(grid);
    }

    record StockItem(String sku, String name, int quantity, double price) {}
}
