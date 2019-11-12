package com.myproject.myspider.crawler.pipeline;

import com.myproject.myspider.model.Comment;
import com.myproject.myspider.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.Map;

@Component("CommentMongodbPipeline")
@Slf4j
public class CommentMongodbPipeline implements Pipeline {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentMongodbPipeline(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
        log.info("CommentMongodbPipeline Completed");
    }

    public void process(ResultItems resultItems, Task task) {
        Map<String, Object> map = resultItems.get("comment");
        String url = resultItems.get("url");
        if (map != null) {
            String productId = (String) map.get("productId");
            if (commentRepository.countByProductId(productId) == 0) {
                String price = (String) map.get("price");
                String date = (String) map.get("date");
                String rate = (String) map.get("rate");
                String productName = (String) map.get("productName");
                String focus = (String) map.get("focus");
                String config = (String) map.get("config");
                Comment item = new Comment(productId, price, date, rate, productName, focus, config, "", "", "", "");
                commentRepository.save(item);
                log.info("保存 "+productId+" 商品信息成功");
            } else {
                Integer type=(Integer)map.get("type");
                Comment comment = commentRepository.findCommentByProductId(productId);
                if(comment!=null&&type!=null){
                    switch (type){
                        case 0:
                            break;
                        case 1:
                            String badReview = (String) map.get("Review");     //score=1
                            if(!comment.getBadReview().contains(badReview))
                                comment.setBadReview(comment.getBadReview()+badReview);
                            break;
                        case 2:
                            String mediaReview = (String) map.get("Review"); //score=2
                            if(!comment.getMediaReview().contains(mediaReview))
                                comment.setMediaReview(comment.getMediaReview()+mediaReview);
                            break;
                        case 3:
                            String goodReview = (String) map.get("Review");   //score=3
                            if(!comment.getGoodReview().contains(goodReview))
                                comment.setGoodReview(comment.getGoodReview()+goodReview);
                            break;
                        case 4:
                            String greatReview = (String) map.get("Review"); //score=4
                            if(!comment.getGreatReview().contains(greatReview))
                                comment.setGreatReview(comment.getGreatReview()+greatReview);
                            break;
                    }
                    commentRepository.save(comment);
                    log.info("保存 "+productId+" 评论信息成功");
                }
                else log.error("保存 "+productId+" 评论信息失败:未找到该商品Id");

            }
            log.info("从URL: " + url + "爬取完毕");
        }

    }
}
