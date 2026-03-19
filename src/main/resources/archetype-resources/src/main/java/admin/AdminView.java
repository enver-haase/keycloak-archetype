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
        add(new H2(getTranslation("admin.title")));
        add(new Paragraph(getTranslation("admin.logged-in-as",
                authenticatedUser.getDisplayName())));
        add(new Paragraph(getTranslation("admin.description")));

        Grid<UserInfo> grid = new Grid<>();
        grid.addColumn(UserInfo::username).setHeader(getTranslation("admin.col.username"));
        grid.addColumn(UserInfo::fullName).setHeader(getTranslation("admin.col.fullname"));
        grid.addColumn(UserInfo::email).setHeader(getTranslation("admin.col.email"));
        grid.addColumn(UserInfo::organizations).setHeader(getTranslation("admin.col.organizations"));

        grid.setItems(
                new UserInfo("alice", "Alice Smith", "alice@example.com", "Blue Corp, Green Inc"),
                new UserInfo("bob", "Bob Jones", "bob@example.com", "Blue Corp"),
                new UserInfo("admin", "Admin User", "admin@example.com", "Blue Corp, Green Inc")
        );

        add(grid);
    }

    record UserInfo(String username, String fullName, String email, String organizations) {}
}
