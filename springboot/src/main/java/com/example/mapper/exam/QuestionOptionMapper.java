package com.example.mapper.exam;

import com.example.entity.exam.QuestionOption;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目选项数据操作接口
 */
public interface QuestionOptionMapper {
    /**
     * 新增题目选项
     */
    int insert(QuestionOption questionOption);

    /**
     * 根据题目ID删除选项
     */
    int deleteByQuestionId(@Param("questionId") Integer questionId);

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
    List<QuestionOption> selectByQuestionId(@Param("questionId") Integer questionId);
}