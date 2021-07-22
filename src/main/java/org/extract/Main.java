package org.extract;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
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
    private static final Path pdfFolder = Paths.get("./PDFs");

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
            Files.createDirectories(pdfFolder);
        } catch (IOException e) {
            logger.fatal("Unable to create download directory", e);
            System.exit(1);
        }
        downloadDirPath = downloadDirPath.resolve("PDFs");
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
                .put("download.default_directory", download_dir)
                .put("plugins.plugins_disabled", new String[] {
                        "Adobe Flash Player",
                        "Chrome PDF Viewer"
                })
                .put("plugins.always_open_pdf_externally", true);
        chromeOptions.setExperimentalOption("prefs", prefs);
        String url = "https://www.paycomonline.net/v4/cl/cl-login.php";
        WebDriver driver = new ChromeDriver(chromeOptions);
        WebDriverWait wait = new WebDriverWait(driver, 30);
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

        ProgressBar progressBar = new ProgressBarBuilder().setUpdateIntervalMillis(1000)
                                                          .setTaskName("Downloading Paystubs")
                                                          .setInitialMax(numOfEmployees)
                                                          .setMaxRenderedLength(120)
                                                          .setStyle(ProgressBarStyle.ASCII)
                                                          .build();
        for (int empCount = 0; empCount < numOfEmployees; empCount++) {

            WebElement empSelectContainer = driver.findElement(By.xpath("//div[@class='empSelect_Container']"));
            String employeeString = empSelectContainer.findElement(By.xpath("./div/div/div[1]/input")).getAttribute("value");
            String eeCode = employeeString.split("\\(|\\)")[1];

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
            waitUntilClickable(driver, By.xpath("//*[@name='enddate']"));
            waitUntilClickable(driver, By.xpath("//*[@name='startdate']"));
            WebElement startDateBox = driver.findElement(By.xpath("//*[@name='startdate']"));
            WebElement endDateBox = driver.findElement(By.xpath("//*[@name='enddate']"));
            startDateBox.sendKeys(startDateInput);
            waitUntilClickable(driver, By.xpath("//*[@name='enddate']"));
            endDateBox.sendKeys(endDateInput);
            driver.findElement(By.xpath("//button[@type='submit']")).click();
            // Date range has been set from 01/01/1900 - 12/31/2021

            // We wait for the files from the "Custom" time period to finish loading. When the loop breaks we move on.
            while (!wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[@id='check-listings-table_processing']")))) {
            }
            List<WebElement> paystubTableRows = driver.findElements(By.xpath("//table[@id='check-listings-table']/tbody/tr[@role='row']"));
            if (paystubTableRows.size() == 0) {
                    waitUntilClickable(driver, By.xpath("//a[@class='cdNextLink']"));
                    continue;
            }

            // Create a directory for each employee code
            Path employeeFolder = employeesFolder.resolve(eeCode);
            try {
                Files.createDirectories(employeeFolder);
            } catch (IOException e) {
                logger.fatal("Unable to create download directory", e);
                System.exit(1);
            }
            // Select all Payroll profiles available
            waitUntilClickable(driver, By.xpath("//input[@id='check-listings-table-select-all']"));
            while (!wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[@id='check-listings-table_processing']")))) {
            }
            waitUntilClickable(driver, By.xpath("//a[@id='viewchecks']"));

            waitUntilFileDownloaded(driver, downloadDirPath.toFile(), 60000, "^.*earnstatement.*pdf$");
            sleep(1000);

            File[] files = downloadDirPath.toFile().listFiles();
            for (File file : files) {
                String downloadName = eeCode + "_earnstatements.pdf";
                if (file.toString().matches("^.*earnstatement.*pdf$")){
                    file.renameTo(employeeFolder.resolve(downloadName).toFile());
                    break;
                }
            }
            try {
                if (downloadDirPath.toFile().exists()) {
                    FileUtils.cleanDirectory(downloadDirPath.toFile());
                    sleep(1000);
                }
            } catch (IOException e) {
                logger.fatal("Cleaning was unsuccessful", e);
                System.exit(1);
            }
            waitUntilClickable(driver, By.xpath("//a[@class='cdNextLink']"));
            //progressBar.step();
        }
        //progressBar.close();
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

    public static void waitUntilFileDownloaded(WebDriver driver, File downloadDir, long timeout, String pattern) {
        FluentWait<WebDriver> wait = new FluentWait<>(driver).withTimeout(Duration.ofMillis(timeout))
                .pollingEvery(Duration.ofMillis(200L));
        RegexFileFilter fileFilter = new RegexFileFilter(pattern);
        wait.until(driver1 -> {
            File[] files = downloadDir.listFiles((FileFilter) fileFilter);
            return (files != null && files.length > 0);
        });
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
