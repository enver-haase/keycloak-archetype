package ${package}.organization;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route("select-organization")
@PermitAll
public class OrganizationSelectorView extends VerticalLayout implements BeforeEnterObserver {

    private final OrganizationService organizationService;

    public OrganizationSelectorView(OrganizationService organizationService) {
        this.organizationService = organizationService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H2 title = new H2("Select Organization");
        Paragraph description = new Paragraph(
                "You belong to multiple organizations. Please select one to continue.");

        List<Organization> orgs = organizationService.getAvailableOrganizations();

        Select<Organization> orgSelect = new Select<>();
        orgSelect.setLabel("Organization");
        orgSelect.setItems(orgs);
        orgSelect.setItemLabelGenerator(Organization::name);
        orgSelect.setWidth("300px");
        if (!orgs.isEmpty()) {
            orgSelect.setValue(orgs.getFirst());
        }

        Button continueButton = new Button("Continue", event -> {
            Organization selected = orgSelect.getValue();
            if (selected != null) {
                organizationService.selectOrganization(selected);
                event.getSource().getUI().ifPresent(ui -> ui.navigate(""));
            }
        });
        continueButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        VerticalLayout card = new VerticalLayout(title, description, orgSelect, continueButton);
        card.setAlignItems(Alignment.CENTER);
        card.setWidth("400px");
        card.getStyle()
                .set("padding", "var(--lumo-space-xl)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-m)");

        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        List<Organization> orgs = organizationService.getAvailableOrganizations();
        if (orgs.size() <= 1) {
            if (orgs.size() == 1) {
                organizationService.selectOrganization(orgs.getFirst());
            }
            event.forwardTo("");
        }
    }
}
