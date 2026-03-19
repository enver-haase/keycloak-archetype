package ${package};

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests organization-scoped navigation filtering, route guards,
 * and redirect-on-switch behavior.
 */
public class OrganizationAccessIT extends AbstractIT {

    @Test
    void blueCorpShowsBlueStockInNav() {
        loginAndSelectOrg("alice", "alice", "Blue Corp");

        List<String> paths = getNavItemPaths();
        assertTrue(paths.contains("blue-stock"), "blue-stock should be in nav");
        assertFalse(paths.contains("green-employees"), "green-employees should NOT be in nav");
    }

    @Test
    void greenIncShowsGreenEmployeesInNav() {
        loginAndSelectOrg("alice", "alice", "Green Inc");

        List<String> paths = getNavItemPaths();
        assertTrue(paths.contains("green-employees"), "green-employees should be in nav");
        assertFalse(paths.contains("blue-stock"), "blue-stock should NOT be in nav");
    }

    @Test
    void blueStockViewRendersGrid() {
        loginAndSelectOrg("alice", "alice", "Blue Corp");
        open("/blue-stock");
        waitFor().until(d -> {
            try { return d.findElement(By.tagName("vaadin-grid")) != null; }
            catch (Exception e) { return false; }
        });

        assertNotNull(getDriver().findElement(By.tagName("vaadin-grid")),
                "Stock grid should be present");
    }

    @Test
    void greenEmployeesViewRendersGrid() {
        loginAndSelectOrg("alice", "alice", "Green Inc");
        open("/green-employees");
        waitFor().until(d -> {
            try { return d.findElement(By.tagName("vaadin-grid")) != null; }
            catch (Exception e) { return false; }
        });

        assertNotNull(getDriver().findElement(By.tagName("vaadin-grid")),
                "Employee grid should be present");
    }

    @Test
    void switchOrgUpdatesNav() {
        loginAndSelectOrg("alice", "alice", "Blue Corp");
        assertTrue(getNavItemPaths().contains("blue-stock"));

        switchOrganization("Green Inc");

        List<String> paths = getNavItemPaths();
        assertTrue(paths.contains("green-employees"), "After switch, green-employees should appear");
        assertFalse(paths.contains("blue-stock"), "After switch, blue-stock should disappear");
    }

    @Test
    void switchOrgFromRestrictedViewRedirectsToDashboard() {
        loginAndSelectOrg("alice", "alice", "Blue Corp");
        open("/blue-stock");
        waitFor().until(d -> {
            try { return d.findElement(By.tagName("vaadin-grid")) != null; }
            catch (Exception e) { return false; }
        });

        switchOrganization("Green Inc");

        waitFor().until(d -> !d.getCurrentUrl().contains("blue-stock"));
        // Should be back on dashboard
        assertTrue(getDriver().findElement(By.tagName("h2")).getText().length() > 0);
    }

    @Test
    void directUrlToRestrictedViewRedirects() {
        loginAndSelectOrg("alice", "alice", "Green Inc");

        open("/blue-stock");

        waitFor().until(d -> {
            try { return d.findElement(By.tagName("h2")) != null; }
            catch (Exception e) { return false; }
        });
        assertFalse(getDriver().getCurrentUrl().contains("blue-stock"),
                "Direct URL to restricted view should redirect");
    }
}
