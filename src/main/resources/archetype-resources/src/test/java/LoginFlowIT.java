package ${package};

import com.vaadin.flow.component.button.testbench.ButtonElement;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the Keycloak login flow, organization selection, and logout.
 */
public class LoginFlowIT extends AbstractIT {

    @Test
    void aliceSeesOrgSelector() {
        keycloakLogin("alice", "alice");
        waitFor().until(d -> {
            try { return d.findElement(By.tagName("h2")) != null; }
            catch (Exception e) { return false; }
        });

        assertTrue(getDriver().getCurrentUrl().contains("select-organization"),
                "Alice (2 orgs) should see the organization selector");
        String pageText = getDriver().findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Blue Corp"), "Should list Blue Corp");
        assertTrue(pageText.contains("Green Inc"), "Should list Green Inc");
    }

    @Test
    void bobSkipsOrgSelector() {
        keycloakLogin("bob", "bob");
        // Bob has only one org and should land on the dashboard directly
        waitFor().until(d -> {
            try {
                return !d.getCurrentUrl().contains("select-organization")
                        && d.findElement(By.tagName("h2")) != null;
            }
            catch (Exception e) { return false; }
        });

        assertFalse(getDriver().getCurrentUrl().contains("select-organization"),
                "Bob should skip the org selector");
    }

    @Test
    void aliceCanSelectOrgAndSeeDashboard() {
        loginAndSelectOrg("alice", "alice", "Blue Corp");

        // Dashboard heading should contain Alice's name
        String heading = getDriver().findElement(By.tagName("h2")).getText();
        assertTrue(heading.contains("Alice Smith"), "Dashboard should greet Alice");
    }

    @Test
    void logoutRedirectsToKeycloak() {
        loginAndSelectOrg("alice", "alice", "Blue Corp");

        $(ButtonElement.class).id("logout-btn").click();

        waitFor().until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        assertTrue(getDriver().getCurrentUrl().contains("realms/"),
                "Should be redirected to Keycloak login");
    }
}
