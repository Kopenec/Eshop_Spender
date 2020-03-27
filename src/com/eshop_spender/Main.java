package com.eshop_spender;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class Main {

    private static ChromeDriver driver;
    private static WebDriverWait wait;

    public static void main(String[] args) {

        // Note 1 - items at alza.cz have to be added to cart individually, we can't select multiple items at once
        // Note 2 - alza.cz is protected by reCAPTCHA and that may unfortunately sometimes stop the the program
        String shop = "http://www.alza.cz";
        String category = "Notebooky";
        String sortByMostExpensive = "Od nejdražšího";
        String backToShopping = "Zpět ke zboží";
        String goToCart = "Pokračovat do košíku";

        System.setProperty("webdriver.chrome.driver", "C:\\Work\\chromedriver_win32\\chromedriver.exe");
        ChromeOptions chrOp = new ChromeOptions();
        chrOp.addArguments("--start-maximized");
        driver = new ChromeDriver(chrOp);
        wait = new WebDriverWait(driver, 7);

        driver.get(shop);
        waitAndClick("//a[@title='" + category + "']");

        // sorting items by price - descending, just so that we don't need to go through all the pages of items
        // to find the most expensive ones
        waitAndClick("//li/a[text()='" + sortByMostExpensive + "']");
        waitForLoader();

        // number of items we want to select and add to cart
        int n = 2;

        // APPROACH 1 (easy - when items can be sorted by price)
        // Items need to be already sorted, first N displayed items is selected, price does not have to be checked.
        for (int i = 1; i <= n; i++) {
            addNthItemToCart(i);
            if (i < n) {
                waitAndClick("//span[text()='" + backToShopping + "']");
                waitForLoader();
            }
        }
        waitAndClick("//span[text()='" + goToCart + "']");


        // APPROACH 2 (harder - when items could not be sorted by price)
        // N most expensive displayed items is selected, price is checked for every item.
        /*
        int thresholdPrice = Integer.MAX_VALUE;
        for (int i = 1; i <= n; i++) {
            thresholdPrice = addMostExpensiveItemToCart(thresholdPrice);
            if (i < n) {
                waitAndClick("//span[text()='" + backToShopping + "']");
                waitForLoader();
            }
        }
        waitAndClick("//span[text()='" + goToCart + "']");
        */
    }



    /*
    Waits until a circular loader disappears (appears and disappears) and the page content is updated.
    Used mainly when switching filter for most expensive or best selling items etc.
     */
    public static void waitForLoader() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//span[@class='circle-loader-container' and starts-with(@style,'display: inline;')]    ")));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(
                "//span[@class='circle-loader-container' and @style='display: none;']")));
    }


    /*
    Waits for an element we want to click on.
     */
    public static void waitAndClick(String item) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(item)));
        driver.findElementByXPath(item).click();
    }


    /*
    Selects the Nth item from the currently displayed items and adds it to cart. Does not check the price.
    Therefore the items need to be already sorted if we want to select the most expensive ones.
     */
    public static void addNthItemToCart(int n) {
        String boxOfItems = "//div[@id='boxes' and @class='browsingitemcontainer']";
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(boxOfItems)));
        WebElement item = driver.findElementByXPath(boxOfItems + "/div[" + n + "]");
        System.out.println("Adding item no. " + n + " with price: " + item.findElement(By.xpath(".//span[@class='c2']")).getText());
        item.findElement(By.xpath(".//a[@class='btnk1']")).click();
    }


    /*
    Selects the most expensive item from currently displayed items with price that is below the threshold price,
    adds the item to cart,
    and returns the price of this selected item.
    (This price can be then used as the next threshold price input for the next most expensive item.)
     */
    public static int addMostExpensiveItemToCart(int thresholdPrice) {
        System.out.println("Threshold price " + thresholdPrice);
        // wait for items on the page to be loaded
        String boxOfItems = "//div[@id='boxes' and @class='browsingitemcontainer']";
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(boxOfItems)));
        // get items - the bottom of each item where price and the buy button are located
        List<WebElement> items = driver.findElementsByXPath(boxOfItems + "/div/div[@class='bottom']");

        String priceStr;
        int i = 1;
        int priceInt = 0;
        int maxPrice = 0;
        WebElement maxItem = items.get(0);
        for (WebElement item : items) {
            // extract price string from the item
            priceStr = item.findElement(By.xpath(".//span[@class='c2']")).getText();
            priceStr = priceStr.substring(0, priceStr.length() - 2);
            priceStr = priceStr.replaceAll(String.valueOf((char) 32), "");
            //convert price string to int
            try {
                priceInt = Integer.parseInt(priceStr.trim());
                System.out.println("Item " + i++ + " price " + priceInt);
            }
            catch (NumberFormatException nfe) {
                System.out.println("NumberFormatException: " + nfe.getMessage());
            }
            // search for max allowed price
            if (priceInt > maxPrice && priceInt < thresholdPrice) {
                maxPrice = priceInt;
                maxItem = item;
            }

        }

        // add selected item to cart
        System.out.println("Adding item with price: " + maxPrice);
        maxItem.findElement(By.xpath(".//a[@class='btnk1']")).click();
        return maxPrice;
    }
}
