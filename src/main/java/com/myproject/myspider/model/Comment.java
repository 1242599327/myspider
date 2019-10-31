package com.myproject.myspider.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Id;

public class Comment implements Serializable {
    @Id
    private String id;
    private String productId;
    private String date;
    private String rate;
    private String productName;
    private List<Map<String, Integer>> focus;

    public Comment(String productId, String date, String rate, String productName, List<Map<String, Integer>> focus) {
        this.productId = productId;
        this.date = date;
        this.rate = rate;
        this.productName = productName;
        this.focus = focus;
    }

    public String toString() {
        return "Comment{id='" + this.id + '\'' + ", productId='" + this.productId + '\'' + ", date='" + this.date + '\'' + ", rate='" + this.rate + '\'' + ", productName='" + this.productName + '\'' + ", focus=" + this.focus + '}';
    }
}
