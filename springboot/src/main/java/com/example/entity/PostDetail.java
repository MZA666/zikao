package com.example.entity;

import java.util.List;

// 添加Reply类的引用
import com.example.entity.Reply;

public class PostDetail {
    private Post post;
    private List<Reply> replies;

    public PostDetail() {}

    public PostDetail(Post post, List<Reply> replies) {
        this.post = post;
        this.replies = replies;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public List<Reply> getReplies() {
        return replies;
    }

    public void setReplies(List<Reply> replies) {
        this.replies = replies;
    }
}