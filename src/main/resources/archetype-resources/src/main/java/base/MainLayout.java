package ${package}.base;

import ${package}.organization.Organization;
import ${package}.organization.OrganizationService;
import ${package}.security.AuthenticatedUser;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final AuthenticatedUser authenticatedUser;
    private final OrganizationService organizationService;

    public MainLayout(AuthenticatedUser authenticatedUser,
                      OrganizationService organizationService) {
        this.authenticatedUser = authenticatedUser;
        this.organizationService = organizationService;

        DrawerToggle toggle = new DrawerToggle();

        H1 appTitle = new H1("${artifactId}");
        appTitle.getStyle()
                .set("font-size", "1.125rem")
                .set("line-height", "2.75rem")
                .set("margin", "0 var(--vaadin-padding-m)");

        SideNav nav = createNavigation();
        nav.getStyle().set("margin", "var(--vaadin-gap-s)");
        Scroller scroller = new Scroller(nav);

        addToDrawer(appTitle, scroller);
        addToNavbar(toggle, createHeader());
        setPrimarySection(Section.DRAWER);
    }

    private HorizontalLayout createHeader() {
        Span userInfo = new Span(authenticatedUser.getDisplayName());

        Span orgInfo = new Span();
        organizationService.getSelectedOrganization()
                .ifPresent(org -> orgInfo.setText(org.name()));

        Button switchOrg = new Button("Switch", event ->
                event.getSource().getUI().ifPresent(ui -> ui.navigate("select-organization")));
        switchOrg.setVisible(organizationService.hasMultipleOrganizations());

        Button logout = new Button("Logout", event -> authenticatedUser.logout());

        HorizontalLayout header = new HorizontalLayout(userInfo, orgInfo, switchOrg, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(orgInfo);
        header.setWidthFull();
        header.getStyle().set("padding", "0 var(--vaadin-padding-m)");

        return header;
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Dashboard", "/"));
        return nav;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (organizationService.hasMultipleOrganizations()
                && organizationService.getSelectedOrganization().isEmpty()) {
            event.forwardTo("select-organization");
        }
    }
}
