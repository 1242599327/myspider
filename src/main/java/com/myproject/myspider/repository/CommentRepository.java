package com.myproject.myspider.repository;

import com.myproject.myspider.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRepository extends MongoRepository<Comment, String> {

    long countByProductId(String productId);
}
