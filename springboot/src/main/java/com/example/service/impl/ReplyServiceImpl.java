package com.example.service.impl;

import com.example.entity.Reply;
import com.example.mapper.ReplyMapper;
import com.example.service.ReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReplyServiceImpl implements ReplyService {

    @Autowired
    private ReplyMapper replyMapper;

    @Override
    public boolean addReply(Reply reply) {
        try {
            int result = replyMapper.addReply(reply);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Reply> getRepliesByPostId(Long postId) {
        try {
            return replyMapper.getRepliesByPostId(postId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteReply(Long id) {
        try {
            int result = replyMapper.deleteReply(id);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Reply getReplyById(Long id) {
        try {
            return replyMapper.getReplyById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}