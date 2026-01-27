package com.example.controller;

import com.example.entity.Reply;
import com.example.service.ReplyService;
import com.example.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reply")
public class ReplyController {

    @Autowired
    private ReplyService replyService;

    /**
     * 添加回复
     */
    @PostMapping("/add")
    public Result addReply(@RequestBody Reply reply) {
        try {
            // 设置默认状态
            reply.setStatus(1);
            boolean success = replyService.addReply(reply);
            if (success) {
                return Result.success("回复成功");
            } else {
                return Result.error("回复失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("回复失败：" + e.getMessage());
        }
    }

    /**
     * 根据帖子ID获取回复列表
     */
    @GetMapping("/list/{postId}")
    public Result getRepliesByPostId(@PathVariable Long postId) {
        try {
            List<Reply> replies = replyService.getRepliesByPostId(postId);
            return Result.success(replies);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取回复列表失败：" + e.getMessage());
        }
    }

    /**
     * 删除回复
     */
    @DeleteMapping("/{id}")
    public Result deleteReply(@PathVariable Long id) {
        try {
            boolean success = replyService.deleteReply(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}