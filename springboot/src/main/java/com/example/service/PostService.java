package com.example.service;

import com.example.entity.Post;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface PostService {
    /**
     * 新增帖子
     */
    void add(Post post);

    /**
     * 删除帖子
     */
    void deleteById(Integer id);

    /**
     * 更新帖子
     */
    void update(Post post);

    /**
     * 根据ID查询帖子
     */
    Post findById(Integer id);

    /**
     * 查询所有帖子
     */
    List<Post> findAll();

    /**
     * 分页查询帖子
     */
    PageInfo<Post> findPage(int pageNum, int pageSize, String title, String startTime, String endTime, Integer userId);

    /**
     * 按用户ID查询帖子
     */
    List<Post> findByUserId(Integer userId);

    /**
     * 查询已发布的帖子（不包括下架的）
     */
    List<Post> findPublishedPosts();

    /**
     * 更新帖子置顶状态
     */
    void updateTopStatus(Integer id, Integer isTop);

    /**
     * 更新帖子下架状态和原因
     */
    void updateOfflineStatus(Integer id, String offlineReason);

    /**
     * 根据关键词搜索帖子（标题和内容）
     */
    List<Post> searchPosts(String keyword);

    /**
     * 根据关键词搜索指定用户的帖子（标题和内容）
     */
    List<Post> searchMyPosts(Integer userId, String keyword);
}