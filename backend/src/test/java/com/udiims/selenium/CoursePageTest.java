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
 * Selenium UI tests for the Course Registration tab in Student Dashboard (UC-02).
 * Login happens once in @BeforeAll; @BeforeEach JS-clicks the Course Registration tab.
 *
 * Run with: mvn test -Pselenium
 */
@DisplayName("Selenium — Course Registration Tab Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CoursePageTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:5173";

    @BeforeAll
    static void setUpAndLogin() {
        System.out.println("[SELENIUM SETUP] CoursePageTest — configuring ChromeDriver");
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
        System.out.println("[SELENIUM SETUP] CoursePageTest — logged in as S001");
    }

    @AfterAll
    static void tearDownDriver() {
        if (driver != null) {
            driver.quit();
            System.out.println("[SELENIUM TEARDOWN] CoursePageTest — driver closed");
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
    @DisplayName("Course Registration tab should be visible and clickable")
    void shouldShowCourseRegistrationTab() {
        System.out.println("[TEST START] CoursePageTest::shouldShowCourseRegistrationTab");
        List<WebElement> tabs = driver.findElements(By.cssSelector(".tab"));
        boolean hasCourseTab = tabs.stream().anyMatch(t -> t.getText().contains("Course Registration"));
        assertTrue(hasCourseTab, "Course Registration tab should exist in navigation");
        System.out.println("[SELENIUM INFO] Tabs: " + tabs.stream().map(WebElement::getText).toList());
        System.out.println("[TEST PASS] CoursePageTest::shouldShowCourseRegistrationTab");
    }

    @Test
    @Order(2)
    @DisplayName("Course Registration tab should show semester term dropdown")
    void shouldHaveSemesterDropdown() {
        System.out.println("[TEST START] CoursePageTest::shouldHaveSemesterDropdown");
        WebElement semesterSelect = driver.findElement(By.cssSelector("select"));
        assertTrue(semesterSelect.isDisplayed(), "Semester dropdown should be visible");
        List<WebElement> options = semesterSelect.findElements(By.tagName("option"));
        assertTrue(options.size() >= 1, "Semester dropdown should have options");
        System.out.println("[SELENIUM INFO] Semester options: " + options.stream().map(WebElement::getText).toList());
        System.out.println("[TEST PASS] CoursePageTest::shouldHaveSemesterDropdown");
    }

    @Test
    @Order(3)
    @DisplayName("Available Courses section should load with course table or empty state")
    void shouldShowAvailableCoursesOrEmptyState() {
        System.out.println("[TEST START] CoursePageTest::shouldShowAvailableCoursesOrEmptyState");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".spinner")));
        boolean hasTable = !driver.findElements(By.cssSelector("table thead tr th")).isEmpty();
        boolean hasEmptyState = !driver.findElements(By.cssSelector(".empty-state")).isEmpty();
        assertTrue(hasTable || hasEmptyState, "Should show course table or empty state message");
        if (hasTable) {
            List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
            System.out.println("[SELENIUM INFO] Available course rows: " + rows.size());
            assertTrue(rows.size() > 0, "Course table should have at least one row");
        } else {
            System.out.println("[SELENIUM INFO] No courses available (empty state shown)");
        }
        System.out.println("[TEST PASS] CoursePageTest::shouldShowAvailableCoursesOrEmptyState");
    }

    @Test
    @Order(4)
    @DisplayName("Available courses table should have Code and Course Name columns")
    void shouldShowCourseCodeAndNameColumns() {
        System.out.println("[TEST START] CoursePageTest::shouldShowCourseCodeAndNameColumns");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".spinner")));
        List<WebElement> headers = driver.findElements(By.cssSelector("table thead tr th"));
        if (!headers.isEmpty()) {
            List<String> headerTexts = headers.stream().map(WebElement::getText).toList();
            System.out.println("[SELENIUM INFO] Table headers: " + headerTexts);
            assertTrue(headerTexts.stream().anyMatch(h -> h.equalsIgnoreCase("code") || h.toUpperCase().contains("CODE")),
                    "Should have a Code column");
            assertTrue(headerTexts.stream().anyMatch(h -> h.toUpperCase().contains("COURSE") || h.toUpperCase().contains("NAME")),
                    "Should have a Course Name column");
        } else {
            System.out.println("[SELENIUM INFO] Table not present — no courses for this semester");
        }
        System.out.println("[TEST PASS] CoursePageTest::shouldShowCourseCodeAndNameColumns");
    }

    @Test
    @Order(5)
    @DisplayName("My Registrations section should be present below available courses")
    void shouldShowMyRegistrationsSection() {
        System.out.println("[TEST START] CoursePageTest::shouldShowMyRegistrationsSection");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".spinner")));
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(),'My Registrations')]")));
        WebElement myRegsTitle = driver.findElement(By.xpath("//*[contains(text(),'My Registrations')]"));
        assertTrue(myRegsTitle.isDisplayed(), "My Registrations section should be visible");
        System.out.println("[TEST PASS] CoursePageTest::shouldShowMyRegistrationsSection");
    }

    @Test
    @Order(6)
    @DisplayName("Page should not have React crash errors")
    void courseRegistrationTabShouldNotCrash() {
        System.out.println("[TEST START] CoursePageTest::courseRegistrationTabShouldNotCrash");
        String pageSource = driver.getPageSource();
        assertFalse(pageSource.contains("Something went wrong"),
                "React error boundary should not trigger on course registration tab");
        System.out.println("[TEST PASS] CoursePageTest::courseRegistrationTabShouldNotCrash");
    }
}
