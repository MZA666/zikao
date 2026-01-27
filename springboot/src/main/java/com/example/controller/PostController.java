package com.example.controller;


import com.example.common.Result;
import com.example.entity.Post;
import com.example.entity.PostDetail;
import com.example.entity.Reply;
import com.example.service.PostService;
import com.example.service.ReplyService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {

    @Resource
    private PostService postService;

    @Resource
    private ReplyService replyService;

    /**
     * 新增帖子
     */
    @PostMapping
    public Result add(@RequestBody Post post) {
        post.setCreateTime(new Date());
        post.setUpdateTime(new Date());
        // 默认状态为已保存（0），如果立即发布则设为已发布（1）
        if (post.getStatus() == null) {
            post.setStatus(0); // 默认为已保存状态
        }
        if (post.getIsTop() == null) {
            post.setIsTop(0); // 默认不置顶
        }
        postService.add(post);
        return Result.success();
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        postService.deleteById(id);
        return Result.success();
    }

    /**
     * 更新帖子
     */
    @PutMapping
    public Result update(@RequestBody Post post) {
        postService.update(post);
        return Result.success();
    }

    /**
     * 根据ID查询帖子
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        Post post = postService.findById(id);
        return Result.success(post);
    }

    /**
     * 学员端 - 根据关键词搜索已发布的帖子（标题和内容）
     */
    @GetMapping("/search")
    public Result searchPublishedPosts(@RequestParam String keyword) {
        List<Post> posts = postService.searchPosts(keyword);
        return Result.success(posts);
    }

    /**
     * 学员端 - 根据关键词搜索我的帖子（标题和内容）
     */
    @GetMapping("/my-posts-search")
    public Result searchMyPosts(@RequestParam Integer userId, @RequestParam String keyword) {
        List<Post> posts = postService.searchMyPosts(userId, keyword);
        return Result.success(posts);
    }

    /**
     * 管理端 - 分页查询帖子
     */
    @GetMapping("/manage/page")
    public Result findPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Integer userId) {
        PageInfo<Post> pageInfo = postService.findPage(pageNum, pageSize, title, startTime, endTime, userId);
        return Result.success(pageInfo);
    }

    /**
     * 学员端 - 查询已发布的帖子
     */
    @GetMapping("/published")
    public Result findPublishedPosts() {
        List<Post> posts = postService.findPublishedPosts();
        return Result.success(posts);
    }

    /**
     * 学员端 - 查询我的帖子
     */
    @GetMapping("/my-posts")
    public Result findMyPosts(@RequestParam Integer userId) {
        List<Post> posts = postService.findByUserId(userId);
        return Result.success(posts);
    }

    /**
     * 管理端 - 置顶/取消置顶帖子
     */
    @PutMapping("/top/{id}")
    public Result updateTopStatus(@PathVariable Integer id, @RequestParam Integer isTop) {
        postService.updateTopStatus(id, isTop);
        return Result.success();
    }

    /**
     * 管理端 - 下架帖子
     */
    @PutMapping("/offline/{id}")
    public Result updateOfflineStatus(@PathVariable Integer id, @RequestParam String offlineReason) {
        postService.updateOfflineStatus(id, offlineReason);
        return Result.success();
    }

    /**
     * 学员端 - 获取帖子详情（包含回复）
     */
    @GetMapping("/detail/{id}")
    public Result getPostDetail(@PathVariable Integer id) {
        try {
            Post post = postService.findById(id);
            if (post == null) {
                return Result.error("帖子不存在");
            }
            
            // 获取该帖子的所有回复
            List<Reply> replies = replyService.getRepliesByPostId(Long.valueOf(id));
            
            // 组装帖子详情
            PostDetail postDetail = new PostDetail(post, replies);
            
            return Result.success(postDetail);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取帖子详情失败：" + e.getMessage());
        }
    }
}