package org.example;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.JDA;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.Duration;

public class PokemonScraper {

    private static final String DISCORD_CHANNEL_ID = ""; // put in the discord channel id here
    private static final List<String> PRODUCTS_TO_TRACK = Arrays.asList(
            "pokemon scarlet violet s3.5 booster bundle box",
            "Pokemon Scarlet & Violet Paldean Fates Booster Pack | Shiny Tinkaton",
            "Pokemon SV8.5 Scarlet and Violet Prismatic Evolutions Booster Pack | Eevee and Sylveon",
            "Pokemon Scarlet & Violet (SV1) Booster Pack | Sprigatito, Fuecoco, & Quaxly"
    );

    private static final List<String> PRODUCT_URLS = Arrays.asList(
            "https://www.target.com/p/pokemon-scarlet-violet-s3-5-booster-bundle-box/-/A-88897904#lnk=sametab",
            "https://www.target.com/p/pokemon-scarlet-violet-paldean-fates-booster-pack-shiny-tinkaton/-/A-93456493#lnk=sametab",
            "https://www.target.com/p/pokemon-sv8-5-scarlet-and-violet-prismatic-evolutions-booster-pack-eevee-and-sylveon/-/A-1001632615#lnk=sametab",
            "https://www.target.com/p/pokemon-scarlet-violet-sv1-booster-pack-sprigatito-fuecoco-quaxly/-/A-1001148311#lnk=sametab"
    );

    private static final Random random = new Random();
    private static final String CHROME_DRIVER_PATH = ""; // Update this path with chromedriver.exe path

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void startCheckingStock(JDA jda) {
        // Set the WebDriver system property
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);

        // Create ChromeOptions and WebDriver instance
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        // options.addArguments("--headless"); // Disable headless mode for debugging purposes
        // options.addArguments("--disable-gpu");

        WebDriver driver = new ChromeDriver(options);

        // Define the task to check stock
        Runnable stockCheckTask = new Runnable() {
            @Override
            public void run() {
                // Log the starting message for the stock check
                System.out.println("\nStarting stock check...\n");

                for (int i = 0; i < PRODUCTS_TO_TRACK.size(); i++) {
                    String productUrl = PRODUCT_URLS.get(i);
                    String productName = PRODUCTS_TO_TRACK.get(i);

                    try {
                        // Open the product page using Selenium
                        driver.get(productUrl);

                        // Wait for the "Out of stock" message or price to appear
                        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                        try {
                            WebElement outOfStockElement = wait.until(
                                    ExpectedConditions.presenceOfElementLocated(By.xpath("//span[contains(text(),'Out of stock')]"))
                            );

                            // If the "Out of stock" span is found, assume the product is out of stock
                            System.out.println("Product is out of stock: " + productName + "\n");
                            continue;
                        } catch (TimeoutException e) {
                            // If the "Out of stock" element is not found, we assume the item is in stock
                            System.out.println("Product is in stock: " + productName + "\n");
                        }

                        // If the item is in stock, retrieve the product price
                        List<WebElement> productPriceElements = driver.findElements(By.cssSelector("span[data-test='product-price']"));
                        String productPrice = "";
                        if (!productPriceElements.isEmpty()) {
                            productPrice = productPriceElements.get(0).getText();
                        }

                        // Send a Discord notification with the price and link
                        if (!productPrice.isEmpty()) {
                            String notificationMessage = String.format("**Product In Stock:**\n%s\nPrice: %s\nLink: %s",
                                    productName, productPrice, productUrl);
                            sendToDiscord(jda, notificationMessage);
                        }

                    } catch (WebDriverException e) {
                        System.err.println("WebDriver issue while scraping: " + productName + "\n");
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.err.println("General error while scraping: " + productName + "\n");
                        e.printStackTrace();
                    }



                    // Introduce a random delay between checks
                    sleepRandomly();
                }

                // Schedule the next execution with a random delay
                long delay = getRandomDelay();
                System.out.println("\nNext stock check scheduled in " + delay + " seconds...\n");
                scheduler.schedule(this, delay, TimeUnit.SECONDS);
            }
        };

        // Schedule the initial execution
        scheduler.schedule(stockCheckTask, 0, TimeUnit.SECONDS);

        // Ensure the driver quits when the program ends
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            driver.quit();
            System.out.println("Selenium WebDriver quit.\n");
        }));
    }

    private static int getRandomDelay() {
        return 1 + random.nextInt(60); // Random delay between 1 and 60 seconds
    }

    private static void sleepRandomly() {
        int minDelay = 500; // Minimum delay in milliseconds
        int maxDelay = 2000; // Maximum delay in milliseconds
        int delay = minDelay + random.nextInt(maxDelay - minDelay);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void sendToDiscord(JDA jda, String message) {
        // Retrieve the channel by ID and send the message
        TextChannel channel = jda.getTextChannelById(DISCORD_CHANNEL_ID);
        if (channel != null) {
            channel.sendMessage(message).queue();
        } else {
            System.err.println("Channel not found with ID: " + DISCORD_CHANNEL_ID + "\n");
        }
    }
}

