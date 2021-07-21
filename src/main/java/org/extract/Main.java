package org.extract;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Thread.sleep;

/**
 * @author Anthony Prestia
 */
public class Main {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final Path employeesFolder = Paths.get("./Employees");

    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver.exe");
        //System.setProperty("webdriveer.chrome.driver", "/usr/local/bin/chromedriver");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please list the directory you want to save the checklist and employee photos: ");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy'\n'hh:mm:ss a");
        SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Path downloadDirPath = null;
        try {
            downloadDirPath = Paths.get(scanner.nextLine()).toFile().getCanonicalFile().toPath();
        } catch (IOException canonical) {
            logger.fatal("Unable to get canonical path", canonical);
            System.exit(1);
        }
        if (!downloadDirPath.toFile().exists()) {
            try {
                Files.createDirectories(downloadDirPath);
            } catch (IOException download) {
                logger.fatal("Unable to create download directory", download);
                System.exit(1);
            }
        }
        try {
            Files.createDirectories(employeesFolder);
        } catch (IOException e) {
            logger.fatal("Unable to create download directory", e);
            System.exit(1);
        }

        String download_dir = downloadDirPath.toString();
        ChromeOptions chromeOptions = new ChromeOptions();
        JSONObject settings = new JSONObject(
                "{\n" +
                        "   \"recentDestinations\": [\n" +
                        "       {\n" +
                        "           \"id\": \"Save as PDF\",\n" +
                        "           \"origin\": \"local\",\n" +
                        "           \"account\": \"\",\n" +
                        "       }\n" +
                        "   ],\n" +
                        "   \"selectedDestinationId\": \"Save as PDF\",\n" +
                        "   \"version\": 2\n" +
                        "}");
        JSONObject prefs = new JSONObject(
                "{\n" +
                        "   \"plugins.plugins_list\":\n" +
                        "       [\n" +
                        "           {\n" +
                        "               \"enabled\": False,\n" +
                        "               \"name\": \"Chrome PDF Viewer\"\n" +
                        "          }\n" +
                        "       ],\n" +
                        "   \"download.extensions_to_open\": \"applications/pdf\"\n" +
                        "}")
                .put("printing.print_preview_sticky_settings.appState", settings)
                .put("download.default_directory", download_dir);
        chromeOptions.setExperimentalOption("prefs", prefs);
        String url = "https://www.paycomonline.net/v4/cl/cl-login.php";
        WebDriver driver = new ChromeDriver(chromeOptions);
        driver.get(url);
        driver.manage().window().maximize();
        System.out.println("Please enter your client code: ");
        String clientCode = scanner.nextLine();
        System.out.println("Please enter your username: ");
        String userName = scanner.nextLine();
        System.out.println("Please enter your password: ");
        String pwd = scanner.nextLine();
        driver.findElement(By.id("clientcode")).sendKeys(clientCode);
        driver.findElement(By.id("txtlogin")).sendKeys(userName);
        driver.findElement(By.id("password")).sendKeys(pwd);
        driver.findElement(By.id("btnSubmit")).click();
        waitForLoad(driver);
        System.out.println(driver.findElement(By.xpath("//*[@id=\"firstSecurityQuestion-row\"]/label")).getText());
        String firstQ = scanner.nextLine();
        driver.findElement(By.xpath("//*[@id=\"firstSecurityQuestion-row\"]/div/div/input")).sendKeys(firstQ);
        System.out.println(driver.findElement(By.xpath("//*[@id=\"secondSecurityQuestion-row\"]/label")).getText());
        String secQ = scanner.nextLine();
        driver.findElement(By.xpath("//*[@id=\"secondSecurityQuestion-row\"]/div/div/input")).sendKeys(secQ);
        driver.findElement(By.xpath("//button[@name='continue']")).click();
        waitForLoad(driver);
        try {
            driver.findElement(By.id("HumanResources"));
        } catch (NoSuchElementException e) {
            logger.fatal("Wrong answers to your questions", e);
            driver.close();
            System.exit(1);
        }
        WebElement elementToHoverOver = driver.findElement(By.id("HumanResources"));
        Actions hover = new Actions(driver).moveToElement(elementToHoverOver);
        hover.perform();
        waitUntilClickable(driver, By.id("DocumentsandChecklists"));
        waitUntilClickable(driver,
                By.xpath("/html/body/div[3]/div/div[2]/div[1]/div[2]/div/div/div[2]/ul/li[2]/a/div[1]")
        );
        List<WebElement> options = driver.findElements(By.xpath(
                "/html/body/div[4]/div/form/div/div/div[1]/div/div[2]/div[1]/div[3]/div/div[2]/div/label/select/option"));
        waitForLoad(driver);
        for (WebElement option : options) {
            if (option.getText().equalsIgnoreCase("500")) {
                waitUntilClickable(driver, option);
                break;
            }
        }
        waitUntilClickable(driver,
                By.xpath(
                        "/html/body/div[4]/div/form/div/div/div[1]/div/div[2]/div[3]/table/tbody/tr[1]/td[2]/a")
        );
        waitUntilClickable(driver, By.xpath("/html/body/div[3]/div/div[2]/div[1]/div/div[2]/div[3]/a"));

        options = driver.findElements(By.xpath(
                "/html/body/div[4]/div/form/div/div[3]/div[1]/div[3]/div/div[2]/div/label/select/option"));
        for (WebElement option : options) {
            if (option.getText().equalsIgnoreCase("500")) {
                waitUntilClickable(driver, option);
                break;
            }
        }
        waitForLoad(driver);
        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "/html/body/div[4]/div/form/div/div[3]/div[3]/table/tbody")));

        // Figure out how many employees are listed
        String rowCountString = driver.findElement(By.xpath("//span[@id='row-count']")).getText();
        int numOfEmployees = Integer.parseInt(rowCountString.split("\\(|\\)")[1]);

        waitUntilClickable(driver, By.xpath("//*[@id=\"make-employee-changes-table\"]/tbody/tr[1]/td[2]/a"));

        waitUntilClickable(driver, By.xpath("/html/body/div[3]/div/div[4]/div/div[2]/div[3]/div[2]/a"));
        // We are on the Year-to-date Totals page

        for (int empCount = 0; empCount < numOfEmployees; empCount++) {

            options = driver.findElements(By.xpath("//select[@id='yearCheck']/option"));
            waitForLoad(driver);
            for (WebElement option : options) {
                if (option.getText().equalsIgnoreCase("Custom")) {
                    waitUntilClickable(driver, option);
                    break;
                }
            }

            String startDateInput = "01011900";
            String endDateInput = "12312021";
            waitUntilClickable(driver, By.xpath("//*[@name='startdate']"));
            WebElement startDateBox = driver.findElement(By.xpath("//*[@name='startdate']"));
            WebElement endDateBox = driver.findElement(By.xpath("//*[@name='enddate']"));
            startDateBox.sendKeys(startDateInput);
            waitUntilClickable(driver, By.xpath("//*[@name='enddate']"));
            endDateBox.sendKeys(endDateInput);
            driver.findElement(By.xpath("//button[@type='submit']")).click();
            // Date range has been set from 01/01/1900 - 12/31/2021

            // WAIT WAIT WAIT It's happening too fast so the list turns out to be 0 most of the time
            List<WebElement> paystubTableRows = driver.findElements(By.xpath("//table[@id='check-listings-table']/tbody/tr[@role='row']"));

            if (paystubTableRows.size() == 0) {
                    waitUntilClickable(driver, By.xpath("//a[@class='cdNextLink']"));
                    continue;
            }
            // HIT THAT M LIKE BUTTON IF YOU UP!

            waitUntilClickable(driver, By.xpath("//input[@id='check-listings-table-select-all']"));
            waitUntilClickable(driver, By.xpath("//a[@id='viewchecks']"));


            waitUntilClickable(driver, By.xpath("//a[@class='cdNextLink']"));
        }

        // End of main
    }

    public static void waitForLoad(WebDriver driver) {
        ExpectedCondition<Boolean> pageLoadCondition = driver1 -> ((JavascriptExecutor) driver1).executeScript(
                "return document.readyState").equals("complete");
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(pageLoadCondition);
    }

    public static void waitUntilClickable(WebDriver driver, By by) {
        WebDriverWait driverWait = new WebDriverWait(driver, 30);
        driverWait.until(ExpectedConditions.elementToBeClickable(by)).click();
        waitForLoad(driver);
    }

    public static void waitUntilClickable(WebDriver driver, WebElement element) {
        WebDriverWait driverWait = new WebDriverWait(driver, 30);
        driverWait.until(ExpectedConditions.elementToBeClickable(element)).click();
        waitForLoad(driver);
    }

}