package com.example.service.exam;

import com.example.entity.exam.QuestionOption;

import java.util.List;

/**
 * 题目选项服务接口
 */
public interface QuestionOptionService {
    /**
     * 新增题目选项
     */
    int insert(QuestionOption questionOption);

    /**
     * 根据题目ID删除选项
     */
    int deleteByQuestionId(Integer questionId);

    /**
     * 更新题目选项
     */
    int updateById(QuestionOption questionOption);

    /**
     * 根据ID查询题目选项
     */
    QuestionOption selectById(Integer id);

    /**
     * 根据题目ID查询所有选项
     */
    List<QuestionOption> selectByQuestionId(Integer questionId);
}