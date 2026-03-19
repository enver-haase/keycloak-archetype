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
        waitFor().until(d -> {
            try { return d.findElement(By.tagName("h2")).getText().contains("Welcome"); }
            catch (Exception e) { return false; }
        });

        String heading = getDriver().findElement(By.tagName("h2")).getText();
        assertTrue(heading.contains("Bob Jones"),
                "Bob should land on the dashboard directly");
    }

    @Test
    void aliceCanSelectOrgAndSeeDashboard() {
        loginAndSelectOrg("alice", "alice", "Blue Corp");

        String heading = getDriver().findElement(By.tagName("h2")).getText();
        assertEquals("Welcome, Alice Smith", heading);
    }

    @Test
    void logoutRedirectsToKeycloak() {
        loginAndSelectOrg("alice", "alice", "Blue Corp");

        $(ButtonElement.class).all().stream()
                .filter(b -> b.getText().trim().equals("Logout"))
                .findFirst()
                .orElseThrow()
                .click();

        waitFor().until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        assertTrue(getDriver().getCurrentUrl().contains("realms/"),
                "Should be redirected to Keycloak login");
    }
}
