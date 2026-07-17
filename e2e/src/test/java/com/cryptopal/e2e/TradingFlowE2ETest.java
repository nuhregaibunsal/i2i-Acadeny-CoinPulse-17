package com.cryptopal.e2e;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

class TradingFlowE2ETest {

    private static final String BASE_URL = System.getProperty("e2e.baseUrl", "http://localhost:5173");

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage",
                    "--disable-gpu", "--window-size=1360,960");
            driver = new ChromeDriver(options);
            wait = new WebDriverWait(driver, Duration.ofSeconds(25));
        } catch (Exception ex) {
            Assumptions.abort("Chrome/driver not available in this environment: " + ex.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void registerLoginBuyAndSeeHolding() {
        String username = "e2e_" + System.currentTimeMillis();

        driver.get(BASE_URL);

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Create one']"))).click();

        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys("secret123");
        driver.findElement(By.xpath("//button[normalize-space()='Create account']")).click();

        WebElement btcRow = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'price-row')][.//span[text()='BTC']]")));
        btcRow.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal")));
        driver.findElement(By.id("volume")).sendKeys("0.05");
        driver.findElement(By.xpath("//button[normalize-space()='Execute Order']")).click();

        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".modal .alert-notice")));
        assertTrue(success.getText().contains("BUY"), "Expected a BUY confirmation, got: " + success.getText());

        driver.findElement(By.xpath("//div[contains(@class,'modal')]//button[@aria-label='Close']")).click();

        WebElement holdingCell = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table[contains(@class,'holdings')]//td[text()='BTC']")));
        assertTrue(holdingCell.isDisplayed(), "BTC holding should appear in the portfolio table");
    }
}
