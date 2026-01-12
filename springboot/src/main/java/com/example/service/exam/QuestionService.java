package com.example.service.exam;

import com.example.entity.exam.Question;

import java.util.List;

public interface QuestionService {
    /**
     * 新增
     */
    int insert(Question question);

    /**
     * 删除
     */
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(Question question);

    /**
     * 根据ID查询
     */
    Question selectById(Integer id);

    /**
     * 查询所有
     */
    List<Question> selectAll(Question question);

    /**
     * 根据学科ID查询
     */
    List<Question> selectBySubjectId(Integer subjectId);

    /**
     * 根据题目类型查询
     */
    List<Question> selectByType(String type);

    /**
     * 随机获取指定数量的题目
     */
    List<Question> selectRandomQuestions(Integer subjectId, String type, Integer limit);

    /**
     * 根据难度随机获取指定数量的题目
     */
    List<Question> selectRandomQuestionsByDifficulty(Integer subjectId, String difficulty, Integer limit);

    /**
     * 根据类型和难度随机获取指定数量的题目
     */
    List<Question> selectRandomQuestionsByTypeAndDifficulty(Integer subjectId, String type, String difficulty, Integer limit);

    /**
     * 查询包含选项的题目
     */
    Question selectQuestionWithOptions(Integer id);
}