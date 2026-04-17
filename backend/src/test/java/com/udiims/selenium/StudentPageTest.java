package com.udiims.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Selenium UI tests for the Student dashboard.
 * Login happens once in @BeforeAll; @BeforeEach navigates to /student.
 * Tabs: Dashboard | Course Registration | GPA & Grades | Program & Backlogs | Fee Status
 */
@DisplayName("Selenium — Student Dashboard Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudentPageTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:5173";

    @BeforeAll
    static void setUpAndLogin() {
        System.out.println("[SELENIUM SETUP] StudentPageTest — configuring ChromeDriver");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1280,720");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Login once for the whole class
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[type='submit']")));
        driver.findElement(By.cssSelector("input[type='text']")).sendKeys("S001");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("pass123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/student"));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".spinner")));
        System.out.println("[SELENIUM SETUP] Logged in as S001, dashboard ready");
    }

    @AfterAll
    static void tearDownDriver() {
        if (driver != null) {
            driver.quit();
            System.out.println("[SELENIUM TEARDOWN] StudentPageTest — driver closed");
        }
    }

    @BeforeEach
    void navigateToDashboard() {
        // Session persists in sessionStorage — just re-navigate to /student
        driver.get(BASE_URL + "/student");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".spinner")));
    }

    @Test
    @Order(1)
    @DisplayName("Student dashboard should load after login")
    void shouldLoadStudentDashboard() {
        System.out.println("[TEST START] StudentPageTest::shouldLoadStudentDashboard");
        assertTrue(driver.getCurrentUrl().contains("/student"),
                "Should be on student dashboard. URL: " + driver.getCurrentUrl());
        assertFalse(driver.getPageSource().contains("Something went wrong"),
                "Should not show a React error boundary");
        System.out.println("[TEST PASS] StudentPageTest::shouldLoadStudentDashboard");
    }

    @Test
    @Order(2)
    @DisplayName("Student dashboard navbar should show UDIIMS branding")
    void shouldShowNavbarBrand() {
        System.out.println("[TEST START] StudentPageTest::shouldShowNavbarBrand");
        WebElement brand = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".navbar-brand")));
        assertTrue(brand.isDisplayed(), "Navbar brand should be visible");
        assertTrue(brand.getText().contains("UDIIMS"), "Brand should say UDIIMS. Got: " + brand.getText());
        System.out.println("[SELENIUM INFO] Navbar brand: " + brand.getText());
        System.out.println("[TEST PASS] StudentPageTest::shouldShowNavbarBrand");
    }

    @Test
    @Order(3)
    @DisplayName("Student dashboard should display the 5 navigation tabs")
    void shouldDisplayFiveTabs() {
        System.out.println("[TEST START] StudentPageTest::shouldDisplayFiveTabs");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".tab")));
        List<WebElement> tabs = driver.findElements(By.cssSelector(".tab"));
        assertTrue(tabs.size() >= 5, "Should have at least 5 tabs. Found: " + tabs.size());
        System.out.println("[SELENIUM INFO] Tabs: " + tabs.stream().map(WebElement::getText).toList());
        System.out.println("[TEST PASS] StudentPageTest::shouldDisplayFiveTabs");
    }

    @Test
    @Order(4)
    @DisplayName("Dashboard tab should show stat cards and student ID in navbar")
    void shouldShowStudentProfileAndStats() {
        System.out.println("[TEST START] StudentPageTest::shouldShowStudentProfileAndStats");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".stat-card")));
        List<WebElement> statCards = driver.findElements(By.cssSelector(".stat-card"));
        assertTrue(statCards.size() >= 3, "Should have at least 3 stat cards. Found: " + statCards.size());

        WebElement userEl = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".navbar-user")));
        assertTrue(userEl.getText().contains("S001"),
                "Navbar should show student ID. Got: " + userEl.getText());
        System.out.println("[SELENIUM INFO] Navbar user: " + userEl.getText());
        System.out.println("[TEST PASS] StudentPageTest::shouldShowStudentProfileAndStats");
    }

    @Test
    @Order(5)
    @DisplayName("Fee Status tab should be clickable and show fee content")
    void shouldShowFeeStatusTab() {
        System.out.println("[TEST START] StudentPageTest::shouldShowFeeStatusTab");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".tab")));
        List<WebElement> tabs = driver.findElements(By.cssSelector(".tab"));
        WebElement feeTab = tabs.stream()
                .filter(t -> t.getText().contains("Fee"))
                .findFirst()
                .orElse(null);
        assertNotNull(feeTab, "Fee Status tab should exist");
        feeTab.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".content")));
        assertFalse(driver.findElement(By.cssSelector(".content")).getText().isEmpty(),
                "Fee content should not be empty after clicking tab");
        System.out.println("[TEST PASS] StudentPageTest::shouldShowFeeStatusTab");
    }
}
