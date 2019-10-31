package com.myproject.myspider.crawler.downloader;

import com.myproject.myspider.crawler.webdriverpool.WebChromeDriverPool;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.PlainText;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class SeleniumChromeDownloader implements Downloader, Closeable {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private volatile WebChromeDriverPool webChromeDriverPool;
    private int sleepTime = 5000;
    private int poolSize = 1;

    public SeleniumChromeDownloader(String chromeDriverPath) {
        System.getProperties().setProperty("webdriver.chrome.driver", chromeDriverPath);
    }

    public SeleniumChromeDownloader() {
        String chromeDriverPath = "G:\\IDEAProject\\myspider\\chromedriver.exe";
        System.getProperties().setProperty("webdriver.chrome.driver", chromeDriverPath);
        log.info("SeleniumChromeDownloader Completed");
    }

    public SeleniumChromeDownloader setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
        return this;
    }

    public Page download(Request request, Task task) {
        this.checkInit();

        WebDriver webDriver;
        try {
            webDriver = this.webChromeDriverPool.get();
        } catch (InterruptedException var10) {
            log.warn("interrupted", var10);
            return null;
        }

        log.info("downloading page " + request.getUrl());
        if (request.getUrl().contains("https://item.jd.com/")) {
            request.setUrl(request.getUrl() + "#comment");
        }

        //webDriver.close();
        webDriver.get(request.getUrl());

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException var9) {
            var9.printStackTrace();
        }

        Options manage = webDriver.manage();
        Site site = task.getSite();
        if (site.getCookies() != null) {
            for (Map.Entry<String, String> cookieEntry : site.getCookies()
                    .entrySet()) {
                Cookie cookie = new Cookie(cookieEntry.getKey(),
                        cookieEntry.getValue());
                manage.addCookie(cookie);
            }
        }

        WebElement webElement = webDriver.findElement(By.xpath("/html"));
        String content = webElement.getAttribute("outerHTML");
        Page page = new Page();
        page.setRawText(content);
        page.setHtml(new Html(content, request.getUrl()));
        page.setUrl(new PlainText(request.getUrl()));
        page.setRequest(request);
        this.webChromeDriverPool.returnToPool(webDriver);
        return page;
    }

    private void checkInit() {
        if (this.webChromeDriverPool == null) {
            synchronized(this) {
                this.webChromeDriverPool = new WebChromeDriverPool(this.poolSize);
            }
        }

    }

    public void setThread(int thread) {
        this.poolSize = thread;
    }

    public void close() throws IOException {
        this.webChromeDriverPool.closeAll();
    }
}
