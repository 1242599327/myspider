package com.myproject.myspider.crawler.pipeline;

import com.myproject.myspider.model.Comment;
import com.myproject.myspider.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;
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
        Map<String, Object> comment = resultItems.get("comment");
        String url=resultItems.get("url");
        if (comment != null) {
            String productId = (String)comment.get("productId");
            if(commentRepository.countByProductId(productId)==0){
                String date = (String)comment.get("date");
                String rate = (String)comment.get("rate");
                String productName = (String)comment.get("productName");
                List<Map<String, Integer>> focus = (List<Map<String, Integer>>) comment.get("focus");
                Comment item = new Comment(productId, date, rate, productName, focus);
                commentRepository.save(item);
                log.info("从URL: " + url + "爬取完毕");
            }else log.info("URL: "+url+" productId duplicate!");
        }

    }
}
