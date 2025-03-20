package com.example.cooksmart_n19.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Comment {
    private String commentId;
    private String postId;
    private String userId;
    private String content;
    @ServerTimestamp
    private Timestamp createdAt;
    @ServerTimestamp
    private Timestamp updatedAt;
}
