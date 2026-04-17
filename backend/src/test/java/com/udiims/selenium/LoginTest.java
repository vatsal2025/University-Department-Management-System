package com.udiims.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Selenium UI tests for the Login page.
 * Requires frontend running at http://localhost:5173
 * and backend running at http://localhost:8080.
 *
 * Run with: mvn test -Dtest="com.udiims.selenium.*"
 */
@DisplayName("Selenium — Login Page Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoginTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:5173";

    // Selectors matched to actual Login.jsx structure
    private static final String ID_INPUT   = "input[type='text']";
    private static final String PASS_INPUT = "input[type='password']";
    private static final String SUBMIT_BTN = "button[type='submit']";
    private static final String ERROR_MSG  = ".alert-error, .alert.alert-error";

    @BeforeAll
    static void setUpDriver() {
        System.out.println("[SELENIUM SETUP] Configuring ChromeDriver via WebDriverManager");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1280,720");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        System.out.println("[SELENIUM SETUP] ChromeDriver ready");
    }

    @AfterAll
    static void tearDownDriver() {
        if (driver != null) {
            driver.quit();
            System.out.println("[SELENIUM TEARDOWN] ChromeDriver closed");
        }
    }

    @BeforeEach
    void navigateToLogin() {
        try {
            ((JavascriptExecutor) driver).executeScript("sessionStorage.clear(); localStorage.clear();");
        } catch (Exception ignored) { }
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(SUBMIT_BTN)));
    }

    @Test
    @Order(1)
    @DisplayName("Login page should render with ID field, password field, and submit button")
    void shouldRenderLoginForm() {
        System.out.println("[TEST START] LoginTest::shouldRenderLoginForm");
        WebElement idInput   = driver.findElement(By.cssSelector(ID_INPUT));
        WebElement passInput = driver.findElement(By.cssSelector(PASS_INPUT));
        WebElement submitBtn = driver.findElement(By.cssSelector(SUBMIT_BTN));

        assertTrue(idInput.isDisplayed(),   "ID input should be visible");
        assertTrue(passInput.isDisplayed(), "Password input should be visible");
        assertTrue(submitBtn.isDisplayed(), "Submit button should be visible");
        System.out.println("[TEST PASS] LoginTest::shouldRenderLoginForm");
    }

    @Test
    @Order(2)
    @DisplayName("Page title should contain UDIIMS")
    void shouldHaveCorrectPageTitle() {
        System.out.println("[TEST START] LoginTest::shouldHaveCorrectPageTitle");
        // The login box header has "UDIIMS" text
        WebElement brand = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(),'UDIIMS')]")));
        assertTrue(brand.isDisplayed(), "UDIIMS branding should be visible");
        System.out.println("[SELENIUM INFO] UDIIMS brand element found: " + brand.getText());
        System.out.println("[TEST PASS] LoginTest::shouldHaveCorrectPageTitle");
    }

    @Test
    @Order(3)
    @DisplayName("Should show error when submitting with empty ID")
    void shouldShowErrorForEmptyId() {
        System.out.println("[TEST START] LoginTest::shouldShowErrorForEmptyId");
        // Leave ID empty, fill password, submit
        driver.findElement(By.cssSelector(PASS_INPUT)).sendKeys("somepassword");
        driver.findElement(By.cssSelector(SUBMIT_BTN)).click();

        // Login.jsx sets error "ID is required." client-side before any API call
        WebElement error = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(ERROR_MSG)));
        assertTrue(error.isDisplayed(), "Error message should appear for empty ID");
        assertTrue(error.getText().contains("required") || error.getText().contains("ID"),
                "Error should mention ID is required. Got: " + error.getText());
        System.out.println("[SELENIUM INFO] Error shown: " + error.getText());
        System.out.println("[TEST PASS] LoginTest::shouldShowErrorForEmptyId");
    }

    @Test
    @Order(4)
    @DisplayName("Should show error for invalid credentials")
    void shouldShowErrorForInvalidCredentials() {
        System.out.println("[TEST START] LoginTest::shouldShowErrorForInvalidCredentials");
        driver.findElement(By.cssSelector(ID_INPUT)).sendKeys("INVALID_XYZ_999");
        driver.findElement(By.cssSelector(PASS_INPUT)).sendKeys("wrongpassword");
        driver.findElement(By.cssSelector(SUBMIT_BTN)).click();

        // Wait for API call to complete and error to show
        WebElement error = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(ERROR_MSG)));
        assertTrue(error.isDisplayed(), "Error message should appear for bad credentials");
        System.out.println("[SELENIUM INFO] Error shown: " + error.getText());
        System.out.println("[TEST PASS] LoginTest::shouldShowErrorForInvalidCredentials");
    }

    @Test
    @Order(5)
    @DisplayName("Should have three role tabs: Student, Dept. Secretary, Finance Officer")
    void shouldHaveThreeRoleTabs() {
        System.out.println("[TEST START] LoginTest::shouldHaveThreeRoleTabs");
        java.util.List<WebElement> tabs = driver.findElements(By.cssSelector(".role-tab"));
        assertEquals(3, tabs.size(), "Should have exactly 3 role tabs");
        System.out.println("[SELENIUM INFO] Role tabs: " + tabs.stream().map(WebElement::getText).toList());
        System.out.println("[TEST PASS] LoginTest::shouldHaveThreeRoleTabs");
    }

    @Test
    @Order(6)
    @DisplayName("Should login as student S001 and redirect to student dashboard")
    void shouldLoginAsStudentAndRedirect() {
        System.out.println("[TEST START] LoginTest::shouldLoginAsStudentAndRedirect");
        driver.findElement(By.cssSelector(ID_INPUT)).sendKeys("S001");
        driver.findElement(By.cssSelector(PASS_INPUT)).sendKeys("pass123");
        driver.findElement(By.cssSelector(SUBMIT_BTN)).click();

        // Should navigate to /student
        wait.until(ExpectedConditions.urlContains("/student"));
        assertTrue(driver.getCurrentUrl().contains("/student"),
                "Should redirect to /student after login. URL: " + driver.getCurrentUrl());
        // Navbar should show student portal text
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".navbar-brand")));
        System.out.println("[SELENIUM INFO] Redirected to: " + driver.getCurrentUrl());
        System.out.println("[TEST PASS] LoginTest::shouldLoginAsStudentAndRedirect");
    }
}
