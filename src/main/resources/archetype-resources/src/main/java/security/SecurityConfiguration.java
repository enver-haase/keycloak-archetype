package ${package}.security;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            ClientRegistrationRepository clientRegistrationRepository) throws Exception {

        OidcClientInitiatedLogoutSuccessHandler logoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        logoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");

        // Permit organization stylesheets (loaded dynamically by MainLayout)
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/**/*.css").permitAll());

        return http.with(VaadinSecurityConfigurer.vaadin(), vaadin -> {
            vaadin.oauth2LoginPage("/oauth2/authorization/keycloak")
                  .logoutSuccessHandler(logoutSuccessHandler);
        }).build();
    }

    /**
     * Maps Keycloak realm roles from the "roles" claim in the ID token
     * to Spring Security granted authorities (ROLE_ prefix).
     * Requires a "realm roles" protocol mapper on the Keycloak client
     * that writes realm roles into a "roles" claim on the ID token.
     */
    @Bean
    GrantedAuthoritiesMapper keycloakAuthoritiesMapper() {
        return authorities -> {
            Set<GrantedAuthority> mapped = new HashSet<>(authorities);
            for (GrantedAuthority authority : authorities) {
                if (authority instanceof OidcUserAuthority oidcAuth) {
                    List<String> roles = oidcAuth.getIdToken().getClaimAsStringList("roles");
                    if (roles != null) {
                        for (String role : roles) {
                            mapped.add(new SimpleGrantedAuthority(
                                    "ROLE_" + role.toUpperCase()));
                        }
                    }
                }
            }
            return mapped;
        };
    }
}
