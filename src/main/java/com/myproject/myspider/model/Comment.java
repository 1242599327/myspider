package com.myproject.myspider.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "comments")
public class Comment implements Serializable {
    @Id
    @Getter
    @Setter
    private String productId;
    @Getter
    @Setter
    private String price;
    @Getter
    @Setter
    private String date;
    @Getter
    @Setter
    private String rate;
    @Getter
    @Setter
    private String productName;
    @Getter
    @Setter
    private String focus;
    @Getter
    @Setter
    private String config;
    @Getter
    @Setter
    private String goodReview;
    @Getter
    @Setter
    private String mediaReview;
    @Getter
    @Setter
    private String badReview;
    @Getter
    @Setter
    private String greatReview;

    public Comment(String productId, String price, String date, String rate, String productName, String focus, String config, String goodReview, String mediaReview, String badReview, String greatReview) {
        this.productId = productId;
        this.price = price;
        this.date = date;
        this.rate = rate;
        this.productName = productName;
        this.focus = focus;
        this.config = config;
        this.goodReview = goodReview;
        this.mediaReview = mediaReview;
        this.badReview = badReview;
        this.greatReview = greatReview;
    }
}
