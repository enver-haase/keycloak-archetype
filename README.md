# Vaadin 25.1 Keycloak Archetype

A Maven archetype for generating Vaadin 25.1.0-beta3 applications with Keycloak OIDC authentication and multi-organization support.

## Features

- **Vaadin 25.1.0-beta3** Flow (server-side Java) with **Aura** theme
- **Spring Boot 4** + **Spring Security OAuth2 Client**
- **Keycloak OIDC** login via `VaadinSecurityConfigurer.oauth2LoginPage()`
- **OIDC-aware logout** that terminates the Keycloak session
- **Multi-organization support**: reads an `organizations` claim (or `groups` fallback) from the ID token; if the user belongs to multiple organizations, a selector view is shown after login; the selected organization is stored in the VaadinSession
- **Switch organization** button in the header to change organization at any time

## Prerequisites

- Java 21+
- Maven 3.9+
- A running Keycloak instance

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
        │   └── OrganizationSelectorView.java
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
| `MainLayout` | App shell with sidebar navigation, user/org display, switch and logout buttons |
| `DashboardView` | Landing page that greets the user and shows the current organization |

## Post-Generation Setup

### 1. Set the Client Secret

Edit `src/main/resources/application.properties` and replace the placeholder:

```properties
spring.security.oauth2.client.registration.keycloak.client-secret=your-secret-here
```

### 2. Configure Keycloak

In your Keycloak admin console:

1. Create a realm (matching `keycloakRealm`)
2. Create a client (matching `keycloakClientId`) with:
   - **Client authentication**: On
   - **Valid redirect URIs**: `http://localhost:8080/login/oauth2/code/keycloak`
   - **Valid post logout redirect URIs**: `http://localhost:8080`
   - **Web origins**: `+`
3. Add an **organizations** claim to the ID token:
   - Go to **Client scopes** > your client's dedicated scope
   - Add a mapper (type: "User Attribute" or "Group Membership")
   - Set **Token claim name** to `organizations`
   - Enable **Add to ID token**

If you skip step 3, the app still works — users simply won't see the organization selector. The `AuthenticatedUser` class also falls back to reading a `groups` claim if `organizations` is not present.

### 3. Run the Application

```bash
cd my-app
./mvnw spring-boot:run
```

Open http://localhost:8080 — you will be redirected to Keycloak to log in.

## Application Flow

1. User navigates to the app
2. Spring Security redirects to Keycloak for OIDC authentication
3. After login, the user is redirected back to the app
4. If the user belongs to multiple organizations, the **Organization Selector** view is shown
5. After selecting an organization (or automatically if only one), the user lands on the **Dashboard**
6. The selected organization is available to all views via `OrganizationService`
7. Users with multiple organizations can switch via the **Switch** button in the header
8. Logout terminates both the Vaadin session and the Keycloak session
