package ${package}.base;

import ${package}.organization.Organization;
import ${package}.organization.OrganizationService;
import ${package}.organization.RequiresOrganization;
import ${package}.security.AuthenticatedUser;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.shared.Registration;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.Map;

@PermitAll
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    /**
     * Maps organization names to their stylesheet paths under
     * {@code src/main/resources/META-INF/resources/}.
     * Add a new entry here when adding a new organization theme.
     */
    private static final Map<String, String> ORG_STYLESHEETS = Map.of(
            "Blue Corp", "blue-corp/styles.css",
            "Green Inc", "green-inc/styles.css"
    );

    private record NavEntry(String label, String route, String requiredOrg, String requiredRole) {
        NavEntry(String label, String route) {
            this(label, route, null, null);
        }
        NavEntry(String label, String route, String requiredOrg) {
            this(label, route, requiredOrg, null);
        }
    }

    private static final List<NavEntry> NAV_ENTRIES = List.of(
            new NavEntry("Dashboard", ""),
            new NavEntry("Blue Stock", "blue-stock", "Blue Corp"),
            new NavEntry("Green Employees", "green-employees", "Green Inc"),
            new NavEntry("Admin", "admin", null, "ADMIN")
    );

    private final AuthenticatedUser authenticatedUser;
    private final OrganizationService organizationService;
    private final Select<Organization> orgSelector;
    private final SideNav nav;

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

        Span userName = new Span(authenticatedUser.getDisplayName());
        userName.getStyle().set("font-weight", "bold");

        orgSelector = new Select<>();
        orgSelector.setItemLabelGenerator(Organization::name);
        List<Organization> orgs = organizationService.getAvailableOrganizations();
        orgSelector.setItems(orgs);
        organizationService.getSelectedOrganization().ifPresent(orgSelector::setValue);
        orgSelector.setVisible(!orgs.isEmpty());
        orgSelector.setWidth("200px");
        orgSelector.addValueChangeListener(event -> {
            if (event.isFromClient() && event.getValue() != null) {
                organizationService.selectOrganization(event.getValue());
                switchOrgStylesheet(event.getValue());
                updateNavigation();
                redirectIfViewForbidden(event.getValue());
            }
        });

        Button logout = new Button("Logout", event -> authenticatedUser.logout());

        HorizontalLayout header = new HorizontalLayout(userName, orgSelector, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(orgSelector);
        header.setWidthFull();
        header.getStyle().set("padding", "0 var(--vaadin-padding-m)");

        nav = new SideNav();
        nav.getStyle().set("margin", "var(--vaadin-gap-s)");
        updateNavigation();
        Scroller scroller = new Scroller(nav);

        addToDrawer(appTitle, scroller);
        addToNavbar(toggle, header);
        setPrimarySection(Section.DRAWER);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        organizationService.getSelectedOrganization().ifPresent(this::switchOrgStylesheet);
    }

    private void updateNavigation() {
        nav.removeAll();
        String selectedOrg = organizationService.getSelectedOrganization()
                .map(Organization::name).orElse("");
        for (NavEntry entry : NAV_ENTRIES) {
            boolean orgMatch = entry.requiredOrg() == null
                    || entry.requiredOrg().equals(selectedOrg);
            boolean roleMatch = entry.requiredRole() == null
                    || authenticatedUser.hasRole(entry.requiredRole());
            if (orgMatch && roleMatch) {
                nav.addItem(new SideNavItem(entry.label(), entry.route()));
            }
        }
    }

    /**
     * Switches the organization stylesheet using the dynamic
     * {@link Registration} pattern. Removes the previous org stylesheet
     * and loads the new one on top of the Aura base theme.
     */
    private void switchOrgStylesheet(Organization org) {
        UI ui = UI.getCurrent();
        if (ui == null) return;

        Registration previous = ComponentUtil.getData(ui, Registration.class);
        if (previous != null) {
            previous.remove();
        }

        String stylesheet = ORG_STYLESHEETS.get(org.name());
        if (stylesheet != null) {
            Registration registration = ui.getPage().addStyleSheet(stylesheet);
            ComponentUtil.setData(ui, Registration.class, registration);
        } else {
            ComponentUtil.setData(ui, Registration.class, null);
        }
    }

    private void redirectIfViewForbidden(Organization newOrg) {
        if (getContent() == null) return;
        RequiresOrganization annotation =
                getContent().getClass().getAnnotation(RequiresOrganization.class);
        if (annotation != null && !annotation.value().equals(newOrg.name())) {
            getContent().getUI().ifPresent(ui -> ui.navigate(""));
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (organizationService.hasMultipleOrganizations()
                && organizationService.getSelectedOrganization().isEmpty()) {
            event.forwardTo("select-organization");
            return;
        }

        RequiresOrganization annotation =
                event.getNavigationTarget().getAnnotation(RequiresOrganization.class);
        if (annotation != null) {
            String selectedOrg = organizationService.getSelectedOrganization()
                    .map(Organization::name).orElse("");
            if (!annotation.value().equals(selectedOrg)) {
                event.forwardTo("");
            }
        }
    }
}
