package com.myproject.myspider;

import com.myproject.myspider.crawler.CommentPageProcessor;
import com.myproject.myspider.crawler.downloader.SeleniumChromeDownloader;
import com.myproject.myspider.crawler.pipeline.CommentMongodbPipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderMonitor;

@SpringBootApplication
@Slf4j
public class MyspiderApplication implements CommandLineRunner {
    //private Logger log = Logger.getLogger(this.getClass());
    private final CommentMongodbPipeline commentMongodbPipeline;
    private final CommentPageProcessor commentPageProcessor;

    @Autowired
    public MyspiderApplication(CommentMongodbPipeline commentMongodbPipeline, CommentPageProcessor commentPageProcessor) {
        this.commentMongodbPipeline = commentMongodbPipeline;
        this.commentPageProcessor = commentPageProcessor;
        log.info("MyspiderApplication Completed");
    }

    public static void main(String[] args) {
        SpringApplication.run(MyspiderApplication.class, args);
    }

    public void run(String... strings) throws Exception {
        Spider spider = Spider.create(commentPageProcessor);
        spider.addUrl("https://list.jd.com/list.html?cat=672&page=1")
                .setDownloader(new SeleniumChromeDownloader())
                .addPipeline(commentMongodbPipeline)
                .thread(5);
        SpiderMonitor.instance().register(spider);
        spider.start();
    }
}
