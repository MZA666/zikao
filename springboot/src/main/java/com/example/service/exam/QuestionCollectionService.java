package com.example.service.exam;

import com.example.entity.exam.QuestionCollection;

import java.util.List;

/**
 * 题目收藏服务接口
 */
public interface QuestionCollectionService {
    /**
     * 新增题目收藏
     */
    int insert(QuestionCollection questionCollection);

    /**
     * 根据ID删除题目收藏
     */
    int deleteById(Integer id);

    /**
     * 根据用户ID和题目ID删除收藏
     */
    int deleteByUserIdAndQuestionId(Integer userId, Integer questionId);

    /**
     * 更新题目收藏
     */
    int updateById(QuestionCollection questionCollection);

    /**
     * 根据ID查询题目收藏
     */
    QuestionCollection selectById(Integer id);

    /**
     * 根据用户ID查询收藏的题目
     */
    List<QuestionCollection> selectByUserId(Integer userId);

    /**
     * 根据题目ID查询收藏记录
     */
    List<QuestionCollection> selectByQuestionId(Integer questionId);

    /**
     * 检查用户是否已收藏题目
     */
    QuestionCollection selectByUserIdAndQuestionId(Integer userId, Integer questionId);

    /**
     * 判断用户是否已收藏题目
     */
    boolean isCollected(Integer userId, Integer questionId);
}