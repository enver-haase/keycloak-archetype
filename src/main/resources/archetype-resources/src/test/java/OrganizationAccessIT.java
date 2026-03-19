package ${package};

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

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

        List<String> nav = getNavItemLabels();
        assertTrue(nav.contains("Blue Stock"), "Blue Stock should be in nav");
        assertFalse(nav.contains("Green Employees"), "Green Employees should NOT be in nav");
    }

    @Test
    void greenIncShowsGreenEmployeesInNav() {
        loginAndSelectOrg("alice", "alice", "Green Inc");

        List<String> nav = getNavItemLabels();
        assertTrue(nav.contains("Green Employees"), "Green Employees should be in nav");
        assertFalse(nav.contains("Blue Stock"), "Blue Stock should NOT be in nav");
    }

    @Test
    void blueStockViewRendersGrid() {
        loginAndSelectOrg("alice", "alice", "Blue Corp");
        open("/blue-stock");
        waitFor().until(d -> {
            try { return d.findElement(By.tagName("h2")).getText().contains("Stock"); }
            catch (Exception e) { return false; }
        });

        String heading = getDriver().findElement(By.tagName("h2")).getText();
        assertEquals("Blue Corp - Stock Management", heading);

        var grid = getDriver().findElement(By.tagName("vaadin-grid"));
        assertNotNull(grid, "Stock grid should be present");
    }

    @Test
    void greenEmployeesViewRendersGrid() {
        loginAndSelectOrg("alice", "alice", "Green Inc");
        open("/green-employees");
        waitFor().until(d -> {
            try { return d.findElement(By.tagName("h2")).getText().contains("Employees"); }
            catch (Exception e) { return false; }
        });

        String heading = getDriver().findElement(By.tagName("h2")).getText();
        assertEquals("Green Inc - Employees", heading);
    }

    @Test
    void switchOrgUpdatesNav() {
        loginAndSelectOrg("alice", "alice", "Blue Corp");
        assertTrue(getNavItemLabels().contains("Blue Stock"));

        switchOrganization("Green Inc");

        List<String> nav = getNavItemLabels();
        assertTrue(nav.contains("Green Employees"), "After switch, Green Employees should appear");
        assertFalse(nav.contains("Blue Stock"), "After switch, Blue Stock should disappear");
    }

    @Test
    void switchOrgFromRestrictedViewRedirectsToDashboard() {
        loginAndSelectOrg("alice", "alice", "Blue Corp");
        open("/blue-stock");
        waitFor().until(d -> {
            try { return d.findElement(By.tagName("h2")).getText().contains("Stock"); }
            catch (Exception e) { return false; }
        });

        switchOrganization("Green Inc");

        waitFor().until(d -> !d.getCurrentUrl().contains("blue-stock"));
        String heading = getDriver().findElement(By.tagName("h2")).getText();
        assertTrue(heading.contains("Welcome"),
                "Should be redirected to dashboard after org switch");
    }

    @Test
    void directUrlToRestrictedViewRedirects() {
        loginAndSelectOrg("alice", "alice", "Green Inc");

        open("/blue-stock");

        waitFor().until(d -> {
            try { return d.findElement(By.tagName("h2")).getText().contains("Welcome"); }
            catch (Exception e) { return false; }
        });
        assertFalse(getDriver().getCurrentUrl().contains("blue-stock"),
                "Direct URL to restricted view should redirect to dashboard");
    }
}
