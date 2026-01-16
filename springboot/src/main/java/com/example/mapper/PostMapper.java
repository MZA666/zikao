package com.example.mapper;

import com.example.entity.Post;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PostMapper {
    /**
     * 新增帖子
     */
    int insert(Post post);

    /**
     * 根据ID删除帖子
     */
    int deleteById(Integer id);

    /**
     * 更新帖子
     */
    int update(Post post);

    /**
     * 根据ID查询帖子
     */
    Post selectById(Integer id);

    /**
     * 查询所有帖子
     */
    List<Post> selectAll();

    /**
     * 查询所有帖子（不分页）
     */
    List<Post> selectAllPosts();

    /**
     * 按条件查询帖子数量
     */
    int selectCount(@Param("title") String title, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("userId") Integer userId);

    /**
     * 按条件查询帖子列表
     */
    List<Post> selectList(@Param("title") String title, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("userId") Integer userId);

    /**
     * 按用户ID查询帖子
     */
    List<Post> selectByUserId(@Param("userId") Integer userId);

    /**
     * 查询已发布的帖子（不包括下架的）
     */
    List<Post> selectPublishedPosts();

    /**
     * 更新帖子置顶状态
     */
    int updateTopStatus(@Param("id") Integer id, @Param("isTop") Integer isTop);

    /**
     * 更新帖子下架状态和原因
     */
    int updateOfflineStatus(@Param("id") Integer id, @Param("offlineReason") String offlineReason);
}