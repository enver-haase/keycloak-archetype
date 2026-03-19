package ${package};

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests role-based access control for the Admin view.
 * The admin user has the "admin" Keycloak realm role;
 * alice and bob do not.
 */
public class AdminAccessIT extends AbstractIT {

    @Test
    void adminSeesAdminNavItem() {
        loginAndSelectOrg("admin", "admin123", "Blue Corp");

        List<String> paths = getNavItemPaths();
        assertTrue(paths.contains("admin"), "Admin user should see the admin nav item");
    }

    @Test
    void adminCanAccessAdminView() {
        loginAndSelectOrg("admin", "admin123", "Blue Corp");
        open("/admin");
        waitFor().until(d -> {
            try { return d.findElement(By.tagName("vaadin-grid")) != null; }
            catch (Exception e) { return false; }
        });

        assertNotNull(getDriver().findElement(By.tagName("vaadin-grid")),
                "Admin user grid should be present");
    }

    @Test
    void aliceDoesNotSeeAdminNavItem() {
        loginAndSelectOrg("alice", "alice", "Blue Corp");

        List<String> paths = getNavItemPaths();
        assertFalse(paths.contains("admin"),
                "Alice (no admin role) should NOT see the admin nav item");
    }

    @Test
    void adminSeesAdminAcrossOrgs() {
        loginAndSelectOrg("admin", "admin123", "Blue Corp");
        assertTrue(getNavItemPaths().contains("admin"),
                "Admin should be visible under Blue Corp");

        switchOrganization("Green Inc");

        assertTrue(getNavItemPaths().contains("admin"),
                "Admin should remain visible after switching to Green Inc");
    }
}
