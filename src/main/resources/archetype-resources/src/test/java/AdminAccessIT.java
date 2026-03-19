package ${package};

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

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

        List<String> nav = getNavItemLabels();
        assertTrue(nav.contains("Admin"), "Admin user should see the Admin nav item");
    }

    @Test
    void adminCanAccessAdminView() {
        loginAndSelectOrg("admin", "admin123", "Blue Corp");
        open("/admin");
        waitFor().until(d -> {
            try { return d.findElement(By.tagName("h2")).getText().contains("Administration"); }
            catch (Exception e) { return false; }
        });

        String heading = getDriver().findElement(By.tagName("h2")).getText();
        assertEquals("Administration", heading);

        // Verify the user grid is present
        var grid = getDriver().findElement(By.tagName("vaadin-grid"));
        assertNotNull(grid, "Admin user grid should be present");
    }

    @Test
    void aliceDoesNotSeeAdminNavItem() {
        loginAndSelectOrg("alice", "alice", "Blue Corp");

        List<String> nav = getNavItemLabels();
        assertFalse(nav.contains("Admin"),
                "Alice (no admin role) should NOT see the Admin nav item");
    }

    @Test
    void adminSeesAdminAcrossOrgs() {
        loginAndSelectOrg("admin", "admin123", "Blue Corp");
        assertTrue(getNavItemLabels().contains("Admin"),
                "Admin should be visible under Blue Corp");

        switchOrganization("Green Inc");

        assertTrue(getNavItemLabels().contains("Admin"),
                "Admin should remain visible after switching to Green Inc");
    }
}
