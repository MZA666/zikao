package com.example.service;

import com.example.entity.Reply;
import java.util.List;

public interface ReplyService {
    /**
     * 添加回复
     */
    boolean addReply(Reply reply);

    /**
     * 根据帖子ID获取回复列表
     */
    List<Reply> getRepliesByPostId(Long postId);

    /**
     * 删除回复
     */
    boolean deleteReply(Long id);

    /**
     * 根据ID获取回复
     */
    Reply getReplyById(Long id);
}