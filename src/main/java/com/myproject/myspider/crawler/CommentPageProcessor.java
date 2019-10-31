package com.myproject.myspider.crawler;

import com.myproject.myspider.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Json;
import us.codecraft.webmagic.selector.JsonPathSelector;
import us.codecraft.webmagic.selector.Selectable;

import java.util.*;

@Component("CommentPageProcessor")
@Slf4j
public class CommentPageProcessor implements PageProcessor {
    private final CommentRepository commentRepository;
    private Site site = Site.me().setCharset("utf-8").setDomain("jingdong.com").setRetryTimes(10).setSleepTime(5000).addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.70 Safari/537.36").addHeader("sec-fetch-mode", "no-cors");

    @Autowired
    public CommentPageProcessor(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
        log.info("CommentPageProcessor Completed");
    }

    public Site getSite() {
        return this.site;
    }

    public void process(Page page) {
        String listUrlFront = "https://list.jd.com/list.html?cat=672&page=";
        String itemUrlFront = "https://item.jd.com/";
        String url = page.getUrl().toString();

        try {
            if (url.contains(itemUrlFront)) {
                Map<String, Object> comment = this.ParseItemAfterRender(page);
                //Map<String, Object> comment = this.ParseItemByJson(page);
                page.putField("comment", comment);
                page.putField("url",url);
            } else if (url.contains(listUrlFront)) {
                this.AddlistUrl(page);
            }
        } catch (Exception e) {
            log.error("出错:" + e.getMessage());
        }

    }

    private Map<String, Object> ParseItemByJson(Page page) {
        String url = page.getUrl().toString();
        String itemUrlFront = "https://item.jd.com/";
        log.info("正在从URL: " + url + "上爬取数据");
        String productId = url.substring(url.indexOf(itemUrlFront) + itemUrlFront.length()).replace(".html#comment", "");
        String rate = page.getHtml().xpath("//*[@id='comment']//div[@class='comment-percent']/div/text()").get();
        String productName = page.getHtml().xpath("//div[@class='item ellipsis']/text()").get();
        List<Map<String, Integer>> focus = new ArrayList();
        List<Selectable> percent_info = page.getHtml().xpath("//*[@id='comment']//div[@class='percent-info']/div/span").nodes();

        Selectable selectable=page.getJson().jsonPath("$.datatype");

        Map<String, Object> comment = new HashMap();
        comment.put("productId", productId);
        comment.put("date", (new Date()).toString());
        comment.put("rate", rate);
        comment.put("productName", productName);
        comment.put("focus", focus);
        return comment;
    }

    private Map<String, Object> ParseItemAfterRender(Page page) throws Exception {
        String url = page.getUrl().toString();
        String itemUrlFront = "https://item.jd.com/";
        log.info("正在从URL: " + url + "上爬取数据");
        String productId = url.substring(url.indexOf(itemUrlFront) + itemUrlFront.length()).replace(".html#comment", "");
        String rate = page.getHtml().xpath("//*[@id='comment']//div[@class='comment-percent']/div/text()").get();
        String productName = page.getHtml().xpath("//div[@class='item ellipsis']/text()").get();
        List<Map<String, Integer>> focus = new ArrayList();
        List<Selectable> percent_info = page.getHtml().xpath("//*[@id='comment']//div[@class='percent-info']/div/span").nodes();
        percent_info.forEach((item) -> {
            String info = item.xpath("/span/text()").get();
            info = info.replaceAll("[\\(|\\)]", ",");
            String feature = info.split(",")[0];
            Integer count = Integer.valueOf(info.split(",")[1]);
            Map<String, Integer> map = new HashMap();
            map.put(feature, count);
            focus.add(map);
        });
        Map<String, Object> comment = new HashMap();
        comment.put("productId", productId);
        comment.put("date", (new Date()).toString());
        comment.put("rate", rate);
        comment.put("productName", productName);
        comment.put("focus", focus);
        return comment;
    }

    
    
    private void AddlistUrl(Page page) {
        String listUrlFront = "https://list.jd.com/list.html?cat=672&page=";
        String url = page.getUrl().toString();
        //Json json = page.getJson();
        //Json jsonp = page.getJson().removePadding("Callback");
        int curr = Integer.parseInt(url.substring(url.indexOf(listUrlFront) + listUrlFront.length()));
        int listnum = Integer.parseInt(page.getHtml().xpath("//*[@id='J_bottomPage']/span[@class='p-skip']/em/b/text()").get());
        if (curr <= listnum) {
            List<String> itemUrls = page.getHtml().xpath("//div[@class='p-name']/a/@href").all();
            page.addTargetRequests(itemUrls);
        }

        if (curr < listnum) {
            ++curr;
            page.addTargetRequest(listUrlFront + curr);
        }

        log.info("新加入url:");
        page.getTargetRequests().forEach((request) -> {
            log.info(request.getUrl());
        });
        log.info("总计:" + page.getTargetRequests().size());
    }

}
