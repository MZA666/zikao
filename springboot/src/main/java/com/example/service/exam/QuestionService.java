package com.example.service.exam;

import com.example.entity.exam.Question;
import com.example.entity.exam.QuestionOption;

import java.util.List;

/**
 * 题目服务接口
 */
public interface QuestionService {
    /**
     * 新增题目
     */
    int insert(Question question);

    /**
     * 根据ID删除题目
     */
    int deleteById(Integer id);

    /**
     * 更新题目
     */
    int updateById(Question question);

    /**
     * 根据ID查询题目
     */
    Question selectById(Integer id);

    /**
     * 查询所有题目
     */
    List<Question> selectAll(Question question);

    /**
     * 根据学科ID查询题目
     */
    List<Question> selectBySubjectId(Integer subjectId);

    /**
     * 根据题目类型查询题目
     */
    List<Question> selectByType(String type);

    /**
     * 根据用户ID查询收藏的题目
     */
    List<Question> selectByUserId(Integer userId);

    /**
     * 获取题目及其选项
     */
    Question selectQuestionWithOptions(Integer id);

    /**
     * 批量插入题目和选项
     */
    int insertQuestionWithOptions(Question question, List<QuestionOption> options);
    
    /**
     * 根据学科ID和类型查询题目
     */
    List<Question> selectBySubjectIdAndType(Integer subjectId, String type);
    
    /**
     * 随机获取指定数量的题目
     */
    List<Question> selectRandomQuestions(Integer subjectId, String type, Integer limit);
}