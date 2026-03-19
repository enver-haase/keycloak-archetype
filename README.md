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
- **Docker Compose** setup with Keycloak and pre-configured realm with test users

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker (for the included Keycloak setup)

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
└── src/main/
    ├── resources/
    │   └── application.properties
    └── java/com/acme/myapp/
        ├── Application.java
        ├── security/
        │   ├── SecurityConfiguration.java
        │   └── AuthenticatedUser.java
        ├── organization/
        │   ├── Organization.java
        │   ├── OrganizationService.java
        │   ├── OrganizationSelectorView.java
        │   └── RequiresOrganization.java
        ├── stock/
        │   └── BlueStockView.java
        ├── employees/
        │   └── GreenEmployeesView.java
        ├── base/
        │   └── MainLayout.java
        └── dashboard/
            └── DashboardView.java
```

### Key Classes

| Class | Description |
|-------|-------------|
| `SecurityConfiguration` | Configures Spring Security with Vaadin's `VaadinSecurityConfigurer`, sets up OAuth2 login and OIDC-aware logout |
| `AuthenticatedUser` | Extracts user info and organization list from the OIDC ID token |
| `OrganizationService` | Manages available organizations and stores the selected one in the VaadinSession |
| `OrganizationSelectorView` | Standalone view shown after login when the user belongs to multiple organizations |
| `MainLayout` | App shell with sidebar navigation, user name, inline org selector, dynamic background color per organization, and logout button |
| `DashboardView` | Landing page that greets the user and shows the current organization |
| `RequiresOrganization` | Annotation to restrict a view to a specific organization (see below) |
| `BlueStockView` | Example org-scoped view: stock grid, only accessible under Blue Corp |
| `GreenEmployeesView` | Example org-scoped view: employee grid, only accessible under Green Inc |

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

| User    | Password | Organizations          |
|---------|----------|------------------------|
| `alice` | `alice`  | Blue Corp, Green Inc   |
| `bob`   | `bob`    | Blue Corp              |

Alice will see the organization selector (she has two orgs). Bob will be taken directly to the dashboard (single org).

The Keycloak admin console is at http://localhost:8180 (admin/admin).

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
