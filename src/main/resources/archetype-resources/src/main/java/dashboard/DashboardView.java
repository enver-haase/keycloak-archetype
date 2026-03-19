package ${package}.dashboard;

import ${package}.base.MainLayout;
import ${package}.organization.OrganizationService;
import ${package}.security.AuthenticatedUser;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PermitAll
public class DashboardView extends VerticalLayout {

    public DashboardView(AuthenticatedUser authenticatedUser,
                         OrganizationService organizationService) {

        H2 welcome = new H2("Welcome, " + authenticatedUser.getDisplayName());
        add(welcome);

        organizationService.getSelectedOrganization().ifPresent(org ->
                add(new Paragraph("Current organization: " + org.name())));
    }
}
