package org.credman0.tcgplayer.purchaser;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.util.List;

public class TCGPlayerInteractor {
    protected Thread workerThread;
    protected WebDriver driver;
    WebDriverWait wait;
    protected String set = "Innistrad";
    public String name = "";
    public TCGPlayerInteractor (File entryFile, String set) {
        this(readEntries(entryFile), set);
    }

    public TCGPlayerInteractor (final String entryString, String set) {
        this.set = set;
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                init(entryString);
            }
        });
        workerThread.start();
    }

    protected void init(String entryString) {
        String chromeDriverPath = "/home/credman0/Downloads/chromedriver" ;
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu", "--window-size=1920,1080","--ignore-certificate-errors");
        options.addArguments("--enable-automation");
        options.addArguments("--enable-automation");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-browser-side-navigation");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, 20);
        driver.get("https://store.tcgplayer.com/massentry");
        WebElement entryArea = driver.findElement(By.tagName("textarea"));
        entryArea.sendKeys(entryString);
        driver.findElement(By.id("addToCartButton")).click();
        waitForCart();
        try {
            optimizeCart();
        } catch (TimeoutException e) {
            // retry once
            optimizeCart();
        }
        driver.findElement(By.id("btnCheckout")).click();
    }

    protected void optimizeCart() throws TimeoutException{
        driver.get("https://store.tcgplayer.com/cartoptimizer?v=2");
        System.out.println(driver.getPageSource());
        wait.until(ExpectedConditions.elementToBeClickable(By.className("toggleMatchPrinting")));
        driver.findElement(By.className("toggleMatchPrinting")).click();
        selectSet();
        driver.findElement(By.className("optimize-button")).click();
        WebDriverWait optimizerWait = new WebDriverWait(driver, 600);
        optimizerWait.until(ExpectedConditions.elementToBeClickable(By.className("select-this-cart-button")));
        driver.findElement(By.xpath("//*[@onclick='SelectDirectCart();']")).click();
        waitForCart();
    }

    protected void waitForCart() {
        wait.until(ExpectedConditions.titleContains("Shopping Cart"));
    }

    protected static String readEntries (File entryFile) {
        String result = "";
        try (FileInputStream in = new FileInputStream(entryFile)) {
            byte[] data = new byte[(int) entryFile.length()];
            in.read(data);
            result = new String(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected void selectSet() {
        driver.findElement(By.className("optimizer-advanced")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.className("tcg-dropdown-container")));
        List<WebElement> productRows = driver.findElements(By.className("productRow"));
        for (WebElement row:productRows) {
            List<WebElement> dropDownDivs = row.findElements(By.className("optWrapper"));
            WebElement setDropDown = null;
            for (WebElement dropDownDiv:dropDownDivs) {
                if (dropDownDiv.findElement(By.className("tcg-dropdown-label")).getText().equals("Sets:")) {
                    setDropDown = dropDownDiv.findElement(By.className("tcg-dropdown-container"));
                    break;
                }
            }
            wait.until(ExpectedConditions.elementToBeClickable(setDropDown));
            setDropDown.click();
            wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(setDropDown, By.tagName("label")));
            wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(setDropDown, By.xpath("//*/label[text() != '']")));
            boolean foundOne = false;
            for (WebElement listElement:setDropDown.findElements(By.tagName("label"))) {
                String labelText = listElement.getText().trim().toLowerCase();
                if (!labelText.equals(set.toLowerCase())) {
                    listElement.findElement(By.xpath("./..")).findElement(By.tagName("input")).click();
                } else {
                    foundOne = true;
                }
            }
            if (!foundOne) {
                System.err.println("Bad set for card:\n" + row.getText());
                for (WebElement listElement:setDropDown.findElements(By.tagName("label"))) {
                    if (!listElement.getText().trim().toLowerCase().contains(set.toLowerCase())) {
                        System.err.println("element " + listElement.getText());
                    }
                }
            }
            // close the dropdown
            setDropDown.click();
        }
    }

    public void join() throws InterruptedException {
        workerThread.join();
    }

    public void getScreenshot(String filename) {
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(screenshot, new File(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
