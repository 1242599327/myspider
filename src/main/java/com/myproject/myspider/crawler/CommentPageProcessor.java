package com.myproject.myspider.crawler;

import com.myproject.myspider.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String reviewUrlFront = "https://club.jd.com/review/";
        String url = page.getUrl().toString();
        try {
            if (url.contains(itemUrlFront)) {
                //商品详情页解析
                ParseItemAfterRender(page);
            } else if (url.contains(reviewUrlFront)) {
                //商品评论页解析
                ParseItemReviewAfterRender(page);
            } else if (url.contains(listUrlFront)) {
                //商品列表页解析
                ParseItemListAfterRender(page);
            } else
                log.warn("无效URL:" + url);
            log.info("从URL:"+url+"中新增的url:");
            page.getTargetRequests().forEach((request) -> {
                log.info(request.getUrl());
            });
            log.info("从URL:"+url+"中新增的url总计:" + page.getTargetRequests().size());
        } catch (Exception e) {
            log.error("出错:" + e.getMessage());
        }

    }

    private void ParseItemReviewAfterRender(Page page) {
        String url = page.getUrl().toString();
        String reviewUrlFront = "https://club.jd.com/review/";
        String[] params = url.replace(reviewUrlFront, "")
                .replace(".html", "")
                .split("-");
        //获取URL信息
        String productId=params[0];
        // 评论页码数 : pageNum
        // 评论类型 ：type, 0：全部评价，1：差评，2：中评，3：好评，4：有晒单
        Integer pageNum = Integer.valueOf(params[2]);
        Integer type=Integer.valueOf(params[3]);
        //获取评论列表
        List<Selectable> reviewlist=page.getHtml().xpath("//div[@id='comments-list']/div[@class='mc']").nodes();
        StringBuilder review=new StringBuilder();
        if(reviewlist.size()!=0){
            reviewlist.forEach((item)->{
                String nickname=item.xpath("//div[@class='i-item']/@data-nickname").get();
                if(nickname!=null){
                    String date_comment=item.xpath("//div[@class='i-item']//div[@class='o-topic']//span[2]/a/text()").get();
                    review.append("{user:'").append(nickname).append("',");
                    review.append("date_comment:'").append(date_comment).append("',");
                    review.append("comment:[");
                    //star.charAt(star.length()-1);
                    List<Selectable> comment_content=item.xpath("//div[@class='comment-content']//dl").nodes();
                    comment_content.forEach((dl)->{
                        String dt=dl.xpath("//dt/text()").get();
                        if(!dt.contains("晒　　单：")) {
                            String dd=dl.xpath("//dd/text()").get();
                            review.append(dt).append(":").append(dd).append(";");
                        }
                    });
                    review.append("]},");
                }
            });
            Map<String, Object> comment = new HashMap<>();
            comment.put("productId",productId);
            comment.put("type",type);
            comment.put("Review",review.toString());
            page.putField("comment", comment);
            page.putField("url", url);
            //添加新的URL
            if(pageNum<=5){
                page.addTargetRequest(reviewUrlFront+productId+"-0-"+(pageNum+1)+"-"+type+".html");
            }
        }

    }

    private void ParseItemAfterRender(Page page) {
        String url = page.getUrl().toString();
        String itemUrlFront = "https://item.jd.com/";
        log.info("正在从URL: " + url + "上爬取数据");
        //爬取页面信息
        String productId = url.substring(url.indexOf(itemUrlFront) + itemUrlFront.length()).replace(".html#comment", "");
        String price = page.getHtml()
                .xpath("//span[@class='price J-p-" + productId + "']/text()").get();
        String rate = page.getHtml().xpath("//*[@id='comment']//div[@class='comment-percent']/div/text()").get();
        String productName = page.getHtml().xpath("//div[@class='item ellipsis']/text()").get();
        StringBuilder focus = new StringBuilder();
        List<Selectable> percent_info = page.getHtml().xpath("//*[@id='comment']//div[@class='percent-info']/div/span").nodes();
        percent_info.forEach((item) -> {
            String info = item.xpath("/span/text()").get();
            info = info.replaceAll("[\\(|\\)]", ",");
            String feature = info.split(",")[0];
            Integer count = Integer.valueOf(info.split(",")[1]);
            focus.append(feature).append(":").append(count).append(";");
        });
        StringBuilder configContent = new StringBuilder();
        List<Selectable> configElements = page.getHtml().xpath("//div[@class='p-parameter']/ul[2]/li").nodes();
        configElements.forEach((element) -> {
            String content = element.xpath("/li/text()").get();
            configContent.append(content).append(";");
        });
        //保存数据
        Map<String, Object> comment = new HashMap<>();
        comment.put("productId", productId);
        comment.put("price", price);
        comment.put("date", (new Date()).toString());
        comment.put("rate", rate);
        comment.put("productName", productName);
        comment.put("focus", focus.toString());
        comment.put("config", configContent.toString());
        //comment.put("Review","");
        page.putField("comment", comment);
        page.putField("url", url);

        //获取页面的url
//        List<Selectable> others = page.getHtml().xpath("//div[@id='choose-attrs']/div[@id='choose-attr-1']/div[@class='dd']/div").nodes();
//        others.forEach((item) -> {
//            String itemId = item.xpath("/div/@data-sku").get();
//            if (!itemId.equals(productId))
//                page.addTargetRequest(itemUrlFront + itemId + ".html");
//        });
        // 添加新的页面评论的url
        // 评论页码数 : pageNum
        // 评论类型 ：type, 0：全部评价，1：差评，2：中评，3：好评，4：有晒单
        int pageNum = 1;
//        String reviewUrlFront = "https://club.jd.com/review/";
//        String reviewUrl = reviewUrlFront +
//                productId +
//                "-0" +
//                "-" + pageNum +
//                "-" + 0 +
//                ".html";
//        page.addTargetRequest(reviewUrl);
        for (int type = 1; type <= 4; type++) {
            String reviewUrlFront = "https://club.jd.com/review/";
            String reviewUrl = reviewUrlFront +
                    productId +
                    "-0" +
                    "-" + pageNum +
                    "-" + type +
                    ".html";
            page.addTargetRequest(reviewUrl);
        }
    }

    private void ParseItemListAfterRender(Page page) {
        String listUrlFront = "https://list.jd.com/list.html?cat=672&page=";
        String url = page.getUrl().toString();
        int curr = Integer.parseInt(url.substring(url.indexOf(listUrlFront) + listUrlFront.length()));
        //int listnum = Integer.parseInt(page.getHtml().xpath("//*[@id='J_bottomPage']/span[@class='p-skip']/em/b/text()").get());
        int listnum=15;
        if (curr <= listnum) {
            List<String> itemUrls = page.getHtml().xpath("//div[@class='p-name']/a/@href").all();
            itemUrls.forEach((r)->{
                if (r.contains("item.jd.com")){
                    page.addTargetRequest(r);
                }
            });
        }
        if (curr < listnum) {
            ++curr;
            page.addTargetRequest(listUrlFront + curr);
        }
    }
}
