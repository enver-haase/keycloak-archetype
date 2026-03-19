package ${package}.admin;

import ${package}.base.MainLayout;
import ${package}.security.AuthenticatedUser;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class AdminView extends VerticalLayout {

    public AdminView(AuthenticatedUser authenticatedUser) {
        add(new H2("Administration"));
        add(new Paragraph("Logged in as: " + authenticatedUser.getDisplayName()));
        add(new Paragraph("This view is only accessible to users with the admin role."));

        Grid<UserInfo> grid = new Grid<>();
        grid.addColumn(UserInfo::username).setHeader("Username");
        grid.addColumn(UserInfo::fullName).setHeader("Full Name");
        grid.addColumn(UserInfo::email).setHeader("Email");
        grid.addColumn(UserInfo::organizations).setHeader("Organizations");

        grid.setItems(
                new UserInfo("alice", "Alice Smith", "alice@example.com", "Blue Corp, Green Inc"),
                new UserInfo("bob", "Bob Jones", "bob@example.com", "Blue Corp"),
                new UserInfo("admin", "Admin User", "admin@example.com", "Blue Corp, Green Inc")
        );

        add(grid);
    }

    record UserInfo(String username, String fullName, String email, String organizations) {}
}
