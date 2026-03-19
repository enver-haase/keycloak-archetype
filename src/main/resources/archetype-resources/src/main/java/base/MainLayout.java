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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@PermitAll
public class MainLayout extends AppLayout implements BeforeEnterObserver, AfterNavigationObserver {

    private static final String[] ORG_COLORS = {
            "#93c5fd", // blue
            "#86efac", // green
            "#fde68a", // amber
            "#f9a8d4", // pink
            "#a5b4fc", // indigo
    };

    private final OrganizationService organizationService;
    private final Select<Organization> orgSelector;

    public MainLayout(AuthenticatedUser authenticatedUser,
                      OrganizationService organizationService) {
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
                applyOrganizationTheme(event.getValue());
            }
        });

        Button logout = new Button("Logout", event -> authenticatedUser.logout());

        HorizontalLayout header = new HorizontalLayout(userName, orgSelector, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(orgSelector);
        header.setWidthFull();
        header.getStyle().set("padding", "0 var(--vaadin-padding-m)");

        SideNav nav = createNavigation();
        nav.getStyle().set("margin", "var(--vaadin-gap-s)");
        Scroller scroller = new Scroller(nav);

        addToDrawer(appTitle, scroller);
        addToNavbar(toggle, header);
        setPrimarySection(Section.DRAWER);
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Dashboard", "/"));
        return nav;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        organizationService.getSelectedOrganization().ifPresent(this::applyOrganizationTheme);
    }

    private void applyOrganizationTheme(Organization org) {
        List<Organization> allOrgs = organizationService.getAvailableOrganizations();
        int index = allOrgs.indexOf(org);
        String color = (index >= 0 && index < ORG_COLORS.length)
                ? ORG_COLORS[index] : "#ffffff";
        getElement().executeJs(
                "var s = this.shadowRoot.querySelector('#org-theme');"
                + "if (!s) { s = document.createElement('style'); s.id = 'org-theme'; this.shadowRoot.appendChild(s); }"
                + "s.textContent = '[content] { background-color: ' + $0 + ' !important;"
                + " transition: background-color 0.5s ease; }';"
                + "var c = this.querySelector(':scope > :not([slot])');"
                + "if (c) { c.style.setProperty('background', 'transparent', 'important'); }",
                color);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (organizationService.hasMultipleOrganizations()
                && organizationService.getSelectedOrganization().isEmpty()) {
            event.forwardTo("select-organization");
        }
    }
}
