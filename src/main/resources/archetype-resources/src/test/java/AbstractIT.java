package ${package};

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.component.sidenav.testbench.SideNavItemElement;
import com.vaadin.testbench.TestBenchTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Base class for TestBench integration tests.
 * <p>
 * TestBench automatically waits for Vaadin server round-trips to complete
 * before every Selenium command (waitForVaadin is built into the driver proxy).
 *
 * <p>Expects the Vaadin app on port 8080 and Keycloak on port 8180.
 *
 * <p>Run with visible Chrome:
 * <pre>mvn verify</pre>
 *
 * <p>Run headless (CI):
 * <pre>mvn verify -Dheadless=true</pre>
 *
 * <p>Auto-start/stop the app via Maven (Keycloak must be running):
 * <pre>mvn verify -Pit -Dheadless=true</pre>
 */
public abstract class AbstractIT extends TestBenchTestCase {

    protected static final String BASE_URL =
            System.getProperty("test.url", "http://localhost:8080");

    @BeforeEach
    void setupDriver() {
        ChromeOptions options = new ChromeOptions();
        // Force English locale so translations match test assertions
        options.addArguments("--lang=en");
        options.setExperimentalOption("prefs", java.util.Map.of("intl.accept_languages", "en"));
        if (Boolean.getBoolean("headless")) {
            options.addArguments("--headless=new", "--disable-gpu", "--no-sandbox",
                    "--window-size=1280,900");
        }
        setDriver(new ChromeDriver(options));
        getDriver().manage().window().setSize(new Dimension(1280, 900));
    }

    @AfterEach
    void teardownDriver() {
        if (getDriver() != null) {
            getDriver().quit();
        }
    }

    protected void open(String path) {
        getDriver().get(BASE_URL + path);
    }

    /**
     * Logs in via Keycloak. Navigates to the app root, waits for the
     * Keycloak login page, fills credentials, and submits.
     */
    protected void keycloakLogin(String username, String password) {
        open("/");
        waitFor().until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        getDriver().findElement(By.id("username")).sendKeys(username);
        getDriver().findElement(By.id("password")).sendKeys(password);
        getDriver().findElement(By.id("kc-login")).click();
    }

    /**
     * On the organization selector view, picks the given org and clicks Continue.
     * Uses TestBench {@link SelectElement} for reliable overlay interaction.
     */
    protected void selectOrganization(String orgName) {
        $(SelectElement.class).first().selectByText(orgName);
        $(ButtonElement.class).id("continue-btn").click();
    }

    /**
     * Logs in and selects an organization in one step.
     * After this call the dashboard is visible.
     */
    protected void loginAndSelectOrg(String username, String password, String org) {
        keycloakLogin(username, password);
        // Wait for Vaadin to fully render after the Keycloak redirect
        waitFor().until(d -> {
            try { return d.findElement(By.tagName("h2")) != null; }
            catch (Exception e) { return false; }
        });
        if (getDriver().getCurrentUrl().contains("select-organization")) {
            selectOrganization(org);
        }
        // Ensure we landed on the dashboard (locale-independent URL check)
        waitFor().until(d -> !d.getCurrentUrl().contains("select-organization"));
    }

    /**
     * Returns the route paths of all side-nav items.
     * Uses paths (e.g., "blue-stock") rather than translated labels
     * so tests work regardless of browser locale.
     */
    protected List<String> getNavItemPaths() {
        return $(SideNavItemElement.class).all().stream()
                .map(item -> item.getAttribute("path"))
                .toList();
    }

    /**
     * Clicks a side-nav item by its visible label text.
     */
    protected void clickNavItem(String label) {
        $(SideNavItemElement.class).all().stream()
                .filter(item -> item.getText().trim().equals(label))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Nav item not found: " + label))
                .click();
        waitFor().until(ExpectedConditions.presenceOfElementLocated(By.tagName("h2")));
    }

    /**
     * Switches the organization via the inline Select in the MainLayout header.
     * Uses TestBench {@link SelectElement} for reliable overlay interaction.
     */
    protected void switchOrganization(String orgName) {
        List<SelectElement> selects = $(SelectElement.class).all();
        SelectElement headerSelect = selects.get(selects.size() - 1);
        headerSelect.selectByText(orgName);
    }

    protected WebDriverWait waitFor() {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(15));
    }
}
