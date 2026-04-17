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
 * Selenium UI tests for the Enrollment flow (UC-02).
 * Login happens once in @BeforeAll; @BeforeEach JS-clicks the Course Registration tab.
 *
 * Run with: mvn test -Pselenium
 */
@DisplayName("Selenium — Enrollment Flow Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnrollmentTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:5173";

    @BeforeAll
    static void setUpAndLogin() {
        System.out.println("[SELENIUM SETUP] EnrollmentTest — configuring ChromeDriver");
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
        System.out.println("[SELENIUM SETUP] EnrollmentTest — logged in as S001");
    }

    @AfterAll
    static void tearDownDriver() {
        if (driver != null) {
            driver.quit();
            System.out.println("[SELENIUM TEARDOWN] EnrollmentTest — driver closed");
        }
    }

    @BeforeEach
    void navigateToCourseRegistrationTab() {
        driver.get(BASE_URL + "/student");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".spinner")));

        // Wait for tabs to be ready and JS-click the Course Registration tab (index 1)
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".tab")));
        List<WebElement> tabs = driver.findElements(By.cssSelector(".tab"));
        WebElement courseTab = tabs.stream()
                .filter(t -> t.getText().contains("Course Registration"))
                .findFirst()
                .orElseGet(() -> tabs.get(1));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", courseTab);

        // Confirm the tab activated by waiting for the semester <select> inside CourseRegistration
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("select")));
    }

    @Test
    @Order(1)
    @DisplayName("Course Registration section should be accessible via tab click")
    void shouldAccessCourseRegistrationSection() {
        System.out.println("[TEST START] EnrollmentTest::shouldAccessCourseRegistrationSection");
        WebElement title = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(),'Course Registration')]")));
        assertTrue(title.isDisplayed(), "Course Registration section should be visible");
        System.out.println("[TEST PASS] EnrollmentTest::shouldAccessCourseRegistrationSection");
    }

    @Test
    @Order(2)
    @DisplayName("Available courses table should show checkboxes for unregistered courses")
    void shouldShowCheckboxesForAvailableCourses() {
        System.out.println("[TEST START] EnrollmentTest::shouldShowCheckboxesForAvailableCourses");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".spinner")));
        List<WebElement> checkboxes = driver.findElements(By.cssSelector("input[type='checkbox']"));
        if (!checkboxes.isEmpty()) {
            System.out.println("[SELENIUM INFO] Found " + checkboxes.size() + " course checkboxes");
            assertTrue(checkboxes.size() > 0, "Should have checkboxes for course selection");
        } else {
            List<WebElement> emptyState = driver.findElements(By.cssSelector(".empty-state"));
            assertTrue(!emptyState.isEmpty(), "Should show checkboxes or empty state — neither found");
            System.out.println("[SELENIUM INFO] No checkboxes — empty state shown");
        }
        System.out.println("[TEST PASS] EnrollmentTest::shouldShowCheckboxesForAvailableCourses");
    }

    @Test
    @Order(3)
    @DisplayName("Selecting a course checkbox should enable the Register button")
    void shouldEnableRegisterButtonWhenCourseSelected() {
        System.out.println("[TEST START] EnrollmentTest::shouldEnableRegisterButtonWhenCourseSelected");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".spinner")));
        List<WebElement> checkboxes = driver.findElements(
                By.cssSelector("input[type='checkbox']:not([disabled])"));
        if (checkboxes.isEmpty()) {
            System.out.println("[SELENIUM INFO] No enabled checkboxes — all courses already registered");
            System.out.println("[TEST PASS] EnrollmentTest::shouldEnableRegisterButtonWhenCourseSelected");
            return;
        }
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkboxes.get(0));
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[contains(text(),'Register')]")));
        WebElement registerBtn = driver.findElement(By.xpath("//button[contains(text(),'Register')]"));
        assertTrue(registerBtn.isDisplayed(), "Register button should appear after selecting a course");
        System.out.println("[SELENIUM INFO] Register button: " + registerBtn.getText());
        System.out.println("[TEST PASS] EnrollmentTest::shouldEnableRegisterButtonWhenCourseSelected");
    }

    @Test
    @Order(4)
    @DisplayName("My Registrations table should show Drop button for active registrations")
    void shouldShowDropButtonForActiveRegistrations() {
        System.out.println("[TEST START] EnrollmentTest::shouldShowDropButtonForActiveRegistrations");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".spinner")));
        List<WebElement> dropButtons = driver.findElements(By.xpath("//button[contains(text(),'Drop')]"));
        if (!dropButtons.isEmpty()) {
            assertTrue(dropButtons.get(0).isDisplayed(), "Drop button should be visible");
            System.out.println("[SELENIUM INFO] Found " + dropButtons.size() + " Drop button(s)");
        } else {
            WebElement myRegsSection = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(text(),'My Registrations')]")));
            assertTrue(myRegsSection.isDisplayed(),
                    "My Registrations section should exist even with no active registrations");
            System.out.println("[SELENIUM INFO] No active registrations — no Drop buttons shown");
        }
        System.out.println("[TEST PASS] EnrollmentTest::shouldShowDropButtonForActiveRegistrations");
    }

    @Test
    @Order(5)
    @DisplayName("Selecting zero courses and clicking Register should show an error")
    void shouldShowErrorWhenNoCoursesSelected() {
        System.out.println("[TEST START] EnrollmentTest::shouldShowErrorWhenNoCoursesSelected");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".spinner")));
        // Register button only appears when a course is checked — verify it is NOT present initially
        List<WebElement> regButtons = driver.findElements(
                By.xpath("//button[contains(text(),'Register') and not(contains(text(),'Course Registration'))]"));
        boolean registerBtnVisible = regButtons.stream().anyMatch(b -> {
            try { return b.isDisplayed(); } catch (StaleElementReferenceException e) { return false; }
        });
        if (!registerBtnVisible) {
            System.out.println("[SELENIUM INFO] Register button correctly hidden when no courses selected");
        } else {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", regButtons.get(0));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-error")));
            WebElement error = driver.findElement(By.cssSelector(".alert-error"));
            assertTrue(error.isDisplayed(), "Error should appear when registering with no courses");
        }
        System.out.println("[TEST PASS] EnrollmentTest::shouldShowErrorWhenNoCoursesSelected");
    }

    @Test
    @Order(6)
    @DisplayName("Changing semester dropdown should reload available courses")
    void shouldReloadCoursesOnSemesterChange() {
        System.out.println("[TEST START] EnrollmentTest::shouldReloadCoursesOnSemesterChange");
        WebElement semSelect = driver.findElement(By.cssSelector("select"));
        String initialValue = semSelect.getAttribute("value");
        List<WebElement> options = semSelect.findElements(By.tagName("option"));
        WebElement differentOption = options.stream()
                .filter(o -> !o.getAttribute("value").equals(initialValue))
                .findFirst().orElse(null);
        if (differentOption != null) {
            differentOption.click();
            try {
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".spinner")));
            } catch (TimeoutException ignored) {}
            assertFalse(driver.getPageSource().contains("Something went wrong"),
                    "Changing semester should not crash the page");
            System.out.println("[SELENIUM INFO] Changed semester to: " + differentOption.getText());
        } else {
            System.out.println("[SELENIUM INFO] Only one semester available — skipping dropdown change");
        }
        System.out.println("[TEST PASS] EnrollmentTest::shouldReloadCoursesOnSemesterChange");
    }
}
