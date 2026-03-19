package ${package}.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides access to the currently authenticated OIDC user and their organizations.
 *
 * Organizations are extracted from the "organizations" claim in the ID token.
 * If that claim is absent, the "groups" claim is used as a fallback.
 *
 * To populate these claims, configure a protocol mapper in Keycloak:
 *   1. In your client settings, go to "Client scopes" > "Dedicated scope"
 *   2. Add a mapper of type "User Attribute" or "Group Membership"
 *   3. Set the token claim name to "organizations"
 *   4. Enable "Add to ID token"
 */
@Component
public class AuthenticatedUser {

    private final AuthenticationContext authenticationContext;

    public AuthenticatedUser(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public Optional<OidcUser> get() {
        return authenticationContext.getAuthenticatedUser(OidcUser.class);
    }

    public String getDisplayName() {
        return get().map(user -> {
            String name = user.getFullName();
            return name != null ? name : user.getPreferredUsername();
        }).orElse("Anonymous");
    }

    @SuppressWarnings("unchecked")
    public List<String> getOrganizations() {
        return get().map(user -> {
            Object orgs = user.getClaim("organizations");
            if (orgs instanceof List<?>) {
                return (List<String>) orgs;
            }
            Object groups = user.getClaim("groups");
            if (groups instanceof List<?>) {
                return (List<String>) groups;
            }
            return Collections.<String>emptyList();
        }).orElse(Collections.emptyList());
    }

    public void logout() {
        authenticationContext.logout();
    }
}
