# Vaadin 25.1 Keycloak Archetype

A Maven archetype for generating Vaadin 25.1.0-beta3 applications with Keycloak OIDC authentication and multi-organization support.

## Features

- **Vaadin 25.1.0-beta3** Flow (server-side Java) with **Aura** theme
- **Spring Boot 4** + **Spring Security OAuth2 Client**
- **Keycloak OIDC** login via `VaadinSecurityConfigurer.oauth2LoginPage()`
- **OIDC-aware logout** that terminates the Keycloak session
- **Multi-organization support**: reads an `organizations` claim (or `groups` fallback) from the ID token; if the user belongs to multiple organizations, a selector view is shown after login; the selected organization is stored in the VaadinSession
- **Inline organization selector** in the header navbar next to the user's name
- **Dynamic background color** per organization: the content area background changes when switching organizations (blue, green, amber, pink, indigo)
- **`@RequiresOrganization` annotation**: restrict views to a specific organization with nav filtering, route guards, and auto-redirect on org switch
- **`@RolesAllowed` with Keycloak realm roles**: admin views secured by Spring Security, with a `GrantedAuthoritiesMapper` that maps Keycloak's `roles` claim to `ROLE_` authorities
- **Docker Compose** setup with Keycloak and pre-configured realm with test users
- **TestBench integration tests**: 15 tests covering login, org access, role access; run headed or headless

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker (for the included Keycloak setup)
- Chrome (for integration tests)

## Install the Archetype

```bash
cd keycloak-vaadin-archetype
mvn clean install
```

## Generate a Project

```bash
mvn archetype:generate \
  -DarchetypeGroupId=com.example \
  -DarchetypeArtifactId=keycloak-vaadin-archetype \
  -DarchetypeVersion=1.0.0-SNAPSHOT \
  -DgroupId=com.acme \
  -DartifactId=my-app \
  -DkeycloakRealm=acme-realm \
  -DkeycloakClientId=acme-client \
  -DkeycloakUrl=http://keycloak.acme.local:8180
```

### Archetype Parameters

| Parameter          | Default                  | Description              |
|--------------------|--------------------------|--------------------------|
| `groupId`          | *(required)*             | Maven group ID           |
| `artifactId`       | *(required)*             | Maven artifact ID        |
| `version`          | `1.0.0-SNAPSHOT`         | Project version          |
| `package`          | same as `groupId`        | Java base package        |
| `keycloakUrl`      | `http://localhost:8180`  | Keycloak server URL      |
| `keycloakRealm`    | `my-realm`               | Keycloak realm name      |
| `keycloakClientId` | `my-vaadin-app`          | OIDC client ID           |

## Generated Project Structure

```
my-app/
├── docker-compose.yml
├── keycloak/
│   └── realm-export.json
├── pom.xml
└── src/
    ├── main/
    │   ├── resources/
    │   │   └── application.properties
    │   └── java/com/acme/myapp/
    │       ├── Application.java
    │       ├── security/
    │       │   ├── SecurityConfiguration.java
    │       │   └── AuthenticatedUser.java
    │       ├── organization/
    │       │   ├── Organization.java
    │       │   ├── OrganizationService.java
    │       │   ├── OrganizationSelectorView.java
    │       │   └── RequiresOrganization.java
    │       ├── stock/
    │       │   └── BlueStockView.java
    │       ├── employees/
    │       │   └── GreenEmployeesView.java
    │       ├── admin/
    │       │   └── AdminView.java
    │       ├── base/
    │       │   └── MainLayout.java
    │       └── dashboard/
    │           └── DashboardView.java
    └── test/java/com/acme/myapp/
        ├── AbstractIT.java
        ├── LoginFlowIT.java
        ├── OrganizationAccessIT.java
        └── AdminAccessIT.java
```

### Key Classes

| Class | Description |
|-------|-------------|
| `SecurityConfiguration` | Configures Spring Security with Vaadin's `VaadinSecurityConfigurer`, sets up OAuth2 login, OIDC-aware logout, and Keycloak realm role mapping |
| `AuthenticatedUser` | Extracts user info, organization list, and roles from the OIDC ID token |
| `OrganizationService` | Manages available organizations and stores the selected one in the VaadinSession |
| `OrganizationSelectorView` | Standalone view shown after login when the user belongs to multiple organizations |
| `MainLayout` | App shell with sidebar navigation, user name, inline org selector, dynamic background color per organization, and logout button |
| `DashboardView` | Landing page that greets the user and shows the current organization |
| `RequiresOrganization` | Custom annotation to restrict a view to a specific organization |
| `BlueStockView` | Example org-scoped view: stock grid, only accessible under Blue Corp |
| `GreenEmployeesView` | Example org-scoped view: employee grid, only accessible under Green Inc |
| `AdminView` | Example role-secured view: user directory grid, secured with `@RolesAllowed("ADMIN")` |

## Quick Start with Docker

The generated project includes a `docker-compose.yml` and a pre-configured Keycloak realm with test users.

```bash
cd my-app

# Start Keycloak (port 8180)
docker compose up -d

# Wait for Keycloak to start (~10s), then run the app
./mvnw spring-boot:run
```

Open http://localhost:8080 and log in with one of the test users:

| User    | Password   | Organizations          | Roles     |
|---------|------------|------------------------|-----------|
| `alice`   | `alice`    | Blue Corp, Green Inc   | *(none)*  |
| `bob`     | `bob`      | Blue Corp              | *(none)*  |
| `admin`   | `admin123` | Blue Corp, Green Inc   | `admin`   |

Alice and Bob are regular users. The `admin` user has the `admin` Keycloak realm role and can access the Admin view.

The Keycloak admin console is at http://localhost:8180 (admin/admin).

## Design Decisions

### Why Vaadin Flow (server-side Java), not Hilla (React)?

This archetype targets **Java-focused teams** building internal business apps. Vaadin Flow lets you write the entire UI in Java with no JavaScript/TypeScript. The Keycloak integration, organization logic, and route guards are all plain Spring beans and Vaadin components — no frontend build chain to maintain.

### Why `VaadinSecurityConfigurer` instead of `VaadinWebSecurity`?

`VaadinWebSecurity` is deprecated in Vaadin 25. The replacement is `VaadinSecurityConfigurer`, which follows Spring Security's composable `HttpSecurity.with(...)` pattern. It configures CSRF, logout, request caching, and navigation access control automatically. The `oauth2LoginPage(...)` method integrates OIDC login with a single line.

### Why `OidcClientInitiatedLogoutSuccessHandler` for logout?

Vaadin's built-in logout only terminates the local session. To also invalidate the Keycloak session (so the user isn't silently re-authenticated), the `OidcClientInitiatedLogoutSuccessHandler` calls Keycloak's `end_session_endpoint`. Without this, clicking "Logout" would redirect back to Keycloak, which would auto-login the user again because the Keycloak session is still alive.

### Why are organizations stored in `VaadinSession` instead of a database?

The selected organization is session-scoped state — it only matters for the current user's current browser session. Storing it in `VaadinSession` keeps the architecture simple (no database table, no JPA entity). The organization *list* comes from the Keycloak ID token claims, so there's no persistence needed for that either. If you need to persist org preferences across sessions, you can extend `OrganizationService` to write to a database.

### Why a custom `@RequiresOrganization` annotation instead of Spring Security roles?

Organizations are **runtime-selected context**, not identity attributes. A user belongs to multiple organizations simultaneously and switches between them. Spring Security's `@RolesAllowed` operates on the user's fixed identity (roles/authorities granted at login time). `@RequiresOrganization` operates on the *currently selected* organization stored in the session. The two mechanisms are complementary: `@RolesAllowed` controls *who* can access a view, `@RequiresOrganization` controls *in which organizational context* it's available.

### Why are Keycloak roles mapped via a `GrantedAuthoritiesMapper` bean?

By default, Spring Security's OAuth2 login does **not** read Keycloak realm roles. Keycloak puts roles in the `realm_access` claim of the access token, but Spring Security reads authorities from the ID token. The `GrantedAuthoritiesMapper` in `SecurityConfiguration` bridges this gap: it reads the `roles` claim (populated by a Keycloak protocol mapper) from the ID token and creates `ROLE_` prefixed Spring Security authorities. This makes `@RolesAllowed("ADMIN")` work with Keycloak's `admin` realm role.

### Why is the `roles` claim a separate protocol mapper?

Keycloak does not include realm roles in the ID token by default. The realm export includes an `oidc-usermodel-realm-role-mapper` that writes realm roles into a `roles` claim on the ID token. This is a deliberate Keycloak configuration, not something Spring Security can infer. If you add new realm roles in Keycloak, they automatically appear in the token via this mapper.

### Why inject CSS into AppLayout's shadow DOM for org background colors?

The Vaadin 25 **Aura theme** applies a surface background to the AppLayout content area via CSS that cannot be overridden by external stylesheets. The Aura theme's `vaadin-app-layout > :nth-child(...)` selector paints a background on the slotted content that wins over any external `background-color`, even with `!important`. Injecting a `<style>` tag into the AppLayout's shadow DOM (targeting `[content]`) and making the slotted view transparent is the only reliable way to change the content area background. This is a workaround for the Aura theme's aggressive surface styling.

### Why `NavEntry` records instead of scanning for annotations?

The side navigation could theoretically be built by scanning the classpath for `@Route` classes and reading their `@RequiresOrganization`/`@RolesAllowed` annotations. This archetype uses an explicit `NavEntry` list instead because:
- It gives full control over ordering, labeling, and grouping
- No classpath scanning overhead at construction time
- Adding a new nav item is a single line, right next to the existing ones
- You can have routes that aren't in the nav (e.g., detail views)

### Why TestBench (not Playwright, Cypress, etc.) for integration tests?

TestBench is Vaadin's official testing framework. Key advantages for Vaadin apps:
- **`SelectElement.selectByText()`** handles Vaadin's custom `<vaadin-select>` overlay reliably, including shadow DOM traversal — a major pain point with plain Selenium
- **Automatic `waitForVaadin`** is built into the TestBench driver proxy (since TestBench 10). Every Selenium command waits for pending server round-trips to complete, eliminating flaky waits
- **Element classes** (`ButtonElement`, `GridElement`, `SideNavItemElement`, etc.) provide high-level APIs for each Vaadin component

The tests use `--headless=new` Chrome mode for CI (the modern headless mode that uses the full rendering engine, unlike the legacy `--headless` which had issues with overlay rendering).

### Why `@BeforeEach`/`@AfterEach` driver management instead of `BrowserTestBase`?

TestBench's `BrowserTestBase` (JUnit 5/6) manages the driver lifecycle and provides `@BrowserTest`. However, combining it with `@SpringBootTest` for auto-starting the app is complex. Managing the `ChromeDriver` manually in `@BeforeEach`/`@AfterEach` while extending `TestBenchTestCase` (for `$()` queries) gives full control over headless flags, window size, and driver options without framework conflicts.

### Why a Maven `it` profile for auto-start/stop?

The `it` profile ties `spring-boot-maven-plugin:start` to `pre-integration-test` and `:stop` to `post-integration-test`. This lets CI pipelines run `mvn verify -Pit -Dheadless=true` with only Docker Keycloak as a prerequisite — Maven handles starting and stopping the Vaadin app around the test phase. Without the profile, `mvn verify` assumes the app is already running, which is more convenient during local development.

## Manual Keycloak Setup

If you prefer to configure Keycloak manually instead of using the Docker setup:

1. Create a realm (matching `keycloakRealm`)
2. Create a client (matching `keycloakClientId`) with:
   - **Client authentication**: On
   - **Client secret**: must match `application.properties`
   - **Valid redirect URIs**: `http://localhost:8080/*`
   - **Valid post logout redirect URIs**: `http://localhost:8080/*`
   - **Web origins**: `+`
3. Add an **organizations** claim to the ID token:
   - Go to **Client scopes** > your client's dedicated scope
   - Add a mapper (type: "User Attribute", multivalued)
   - Set **Token claim name** to `organizations`
   - Enable **Add to ID token**
4. Add a **roles** claim to the ID token:
   - Add a mapper (type: "User Realm Role")
   - Set **Token claim name** to `roles`
   - Enable **Add to ID token** and **Multivalued**

If you skip step 3, the app still works — users simply won't see the organization selector. The `AuthenticatedUser` class also falls back to reading a `groups` claim if `organizations` is not present.

## Organization-Scoped Views

Views can be restricted to a specific organization using the `@RequiresOrganization` annotation:

```java
@Route(value = "blue-stock", layout = MainLayout.class)
@PermitAll
@RequiresOrganization("Blue Corp")
public class BlueStockView extends VerticalLayout {
    // only accessible when Blue Corp is selected
}
```

Three mechanisms enforce the restriction:

1. **Navigation guard** — `MainLayout.beforeEnter()` checks the annotation on the target view. If the selected organization doesn't match, the user is forwarded to the dashboard.
2. **Side nav filtering** — Navigation items are only shown for views matching the current organization. To register a new org-scoped view in the nav, add a `NavEntry` in `MainLayout`:
   ```java
   private static final List<NavEntry> NAV_ENTRIES = List.of(
           new NavEntry("Dashboard", ""),
           new NavEntry("Blue Stock", "blue-stock", "Blue Corp"),
           new NavEntry("Green Employees", "green-employees", "Green Inc")
   );
   ```
3. **Org-switch redirect** — If the user switches organization while viewing an org-restricted page, they are automatically redirected to the dashboard.

### Included example views

| View | Route | Organization | Content |
|------|-------|--------------|---------|
| `BlueStockView` | `/blue-stock` | Blue Corp | Stock management grid (SKU, product, quantity, price) |
| `GreenEmployeesView` | `/green-employees` | Green Inc | Employee directory grid (ID, name, department, email) |

## Role-Based Views

Views can be secured by Keycloak realm roles using Spring Security's standard `@RolesAllowed` annotation:

```java
@Route(value = "admin", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class AdminView extends VerticalLayout {
    // only accessible to users with the "admin" Keycloak realm role
}
```

The archetype includes a `GrantedAuthoritiesMapper` bean in `SecurityConfiguration` that maps Keycloak realm roles from the `roles` ID token claim to Spring Security `ROLE_` authorities. This requires the "realm roles" protocol mapper on the Keycloak client (included in the Docker realm export).

Nav items can also be role-filtered by adding a `requiredRole` to the `NavEntry`:

```java
new NavEntry("Admin", "admin", null, "ADMIN")  // null org = any org, "ADMIN" = requires ADMIN role
```

## Integration Tests

The generated project includes 15 TestBench integration tests in three suites:

| Suite | Tests | Covers |
|-------|-------|--------|
| `LoginFlowIT` | 4 | Keycloak login, org selector for multi-org users, single-org auto-select, logout |
| `OrganizationAccessIT` | 7 | Nav filtering per org, view grid rendering, org switch updates nav, switch-from-restricted redirects, direct URL route guard |
| `AdminAccessIT` | 4 | Admin nav visible for admin role, admin view access, hidden for non-admins, admin persists across org switch |

### Running Tests

```bash
# Visible Chrome (app + Keycloak must be running)
mvn verify

# Headless Chrome for CI
mvn verify -Dheadless=true

# Auto-start/stop the app (Keycloak must be running)
mvn verify -Pit -Dheadless=true
```

## Application Flow

1. User navigates to the app
2. Spring Security redirects to Keycloak for OIDC authentication
3. After login, the user is redirected back to the app
4. If the user belongs to multiple organizations, the **Organization Selector** view is shown
5. After selecting an organization (or automatically if only one), the user lands on the **Dashboard**
6. The content area background changes to the organization's color (blue, green, amber, pink, or indigo based on org index)
7. The selected organization is available to all views via `OrganizationService`
8. Users with multiple organizations can switch via the inline selector in the header
9. Logout terminates both the Vaadin session and the Keycloak session
