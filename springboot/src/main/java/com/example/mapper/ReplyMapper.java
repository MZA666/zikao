package com.example.mapper;

import com.example.entity.Reply;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ReplyMapper {
    /**
     * 添加回复
     */
    @Insert("INSERT INTO reply(post_id, user_id, username, content, status) VALUES(#{postId}, #{userId}, #{username}, #{content}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addReply(Reply reply);

    /**
     * 根据帖子ID获取回复列表
     */
    @Select("SELECT * FROM reply WHERE post_id = #{postId} AND status = 1 ORDER BY create_time ASC")
    List<Reply> getRepliesByPostId(@Param("postId") Long postId);

    /**
     * 根据回复ID删除回复
     */
    @Update("UPDATE reply SET status = 0 WHERE id = #{id}")
    int deleteReply(@Param("id") Long id);

    /**
     * 根据回复ID永久删除回复
     */
    @Delete("DELETE FROM reply WHERE id = #{id}")
    int permanentDeleteReply(@Param("id") Long id);

    /**
     * 根据回复ID获取回复
     */
    @Select("SELECT * FROM reply WHERE id = #{id} AND status = 1")
    Reply getReplyById(@Param("id") Long id);
}