package com.myproject.myspider.crawler.webdriverpool;


import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class WebChromeDriverPool {
    private final Logger logger= LoggerFactory.getLogger(getClass());
    private static final int DEFAULT_CAPACITY = 5;
    private final int capacity;
    private static final int STAT_RUNNING = 1;
    private static final int STAT_CLODED = 2;
    private AtomicInteger stat= new AtomicInteger(STAT_RUNNING);
    private WebDriver mDriver;
    protected static DesiredCapabilities sCaps;
    /**
     * store webDrivers created
     */
    private List<WebDriver> webDriverList = Collections
            .synchronizedList(new ArrayList<WebDriver>());

    /**
     * store webDrivers available
     */
    private BlockingDeque<WebDriver> innerQueue = new LinkedBlockingDeque<WebDriver>();


    public void configure() throws IOException {
        sCaps = new DesiredCapabilities();
        sCaps.setJavascriptEnabled(true);
        sCaps.setCapability("takesScreenshot", false);

        ChromeOptions options = new ChromeOptions();
        //options.addArguments("user-data-dir='G:\\IDEAProject\\myspider\\ChromeUserData'");
        options.addArguments("process-per-site");
        options.setHeadless(true);
        options.addArguments("blink-settings=imagesEnabled=false");
        sCaps.setCapability(ChromeOptions.CAPABILITY,options);
        this.mDriver = new ChromeDriver(sCaps);
    }

    private boolean isUrl(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (MalformedURLException var3) {
            return false;
        }
    }

    public WebChromeDriverPool(int capacity) {
        this.capacity = capacity;
    }

    public WebChromeDriverPool() {
        this(DEFAULT_CAPACITY);
        logger.info("WebChromeDriverPool Start");
    }

    /**
     *
     * @return
     * @throws InterruptedException
     */
    public WebDriver get() throws InterruptedException {
        checkRunning();
        WebDriver poll = innerQueue.poll();
        if (poll != null) {
            return poll;
        }
        if (webDriverList.size() < capacity) {
            synchronized (webDriverList) {
                if (webDriverList.size() < capacity) {

                    // add new WebDriver instance into pool
                    try {
                        configure();
                        innerQueue.add(mDriver);
                        webDriverList.add(mDriver);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // ChromeDriver e = new ChromeDriver();
                    // WebDriver e = getWebDriver();
                    // innerQueue.add(e);
                    // webDriverList.add(e);
                }
            }

        }
        return innerQueue.take();
    }

    public void returnToPool(WebDriver webDriver) {
        checkRunning();
        innerQueue.add(webDriver);
    }

    protected void checkRunning() {
        if (!stat.compareAndSet(STAT_RUNNING, STAT_RUNNING)) {
            throw new IllegalStateException("Already closed!");
        }
    }


    public void closeAll() {
        boolean b = stat.compareAndSet(STAT_RUNNING, STAT_CLODED);
        if (!b) {
            throw new IllegalStateException("Already closed!");
        }
        for (WebDriver webDriver : webDriverList) {
            logger.info("Quit webDriver" + webDriver);
            webDriver.quit();
            webDriver = null;
        }
    }

}
