package ${package}.organization;

import ${package}.security.AuthenticatedUser;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizationService {

    private static final String SESSION_KEY = "selected-organization";

    private final AuthenticatedUser authenticatedUser;

    public OrganizationService(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    public List<Organization> getAvailableOrganizations() {
        return authenticatedUser.getOrganizations().stream()
                .map(name -> new Organization(name, name))
                .toList();
    }

    public void selectOrganization(Organization organization) {
        VaadinSession.getCurrent().setAttribute(SESSION_KEY, organization);
    }

    public Optional<Organization> getSelectedOrganization() {
        return Optional.ofNullable(
                (Organization) VaadinSession.getCurrent().getAttribute(SESSION_KEY));
    }

    public boolean hasMultipleOrganizations() {
        return getAvailableOrganizations().size() > 1;
    }
}
