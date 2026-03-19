package ${package}.employees;

import ${package}.base.MainLayout;
import ${package}.organization.RequiresOrganization;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "green-employees", layout = MainLayout.class)
@PermitAll
@RequiresOrganization("Green Inc")
public class GreenEmployeesView extends VerticalLayout {

    public GreenEmployeesView() {
        add(new H2("Green Inc - Employees"));

        Grid<Employee> grid = new Grid<>();
        grid.addColumn(Employee::id).setHeader("ID");
        grid.addColumn(Employee::name).setHeader("Name");
        grid.addColumn(Employee::department).setHeader("Department");
        grid.addColumn(Employee::email).setHeader("Email");

        grid.setItems(
                new Employee("GI-001", "Jane Doe", "Engineering", "jane@greeninc.com"),
                new Employee("GI-002", "John Roe", "Marketing", "john@greeninc.com"),
                new Employee("GI-003", "Pat Moe", "Finance", "pat@greeninc.com")
        );

        add(grid);
    }

    record Employee(String id, String name, String department, String email) {}
}
