package com.example.service.exam;

import com.example.entity.exam.ExamPaperQuestion;

import java.util.List;

/**
 * 试卷题目关联服务接口
 */
public interface ExamPaperQuestionService {
    /**
     * 新增试卷题目关联
     */
    int insert(ExamPaperQuestion examPaperQuestion);

    /**
     * 根据试卷ID删除关联
     */
    int deleteByExamPaperId(Integer examPaperId);

    /**
     * 根据试卷ID和题目ID删除关联
     */
    int deleteByExamPaperIdAndQuestionId(Integer examPaperId, Integer questionId);

    /**
     * 更新试卷题目关联
     */
    int updateById(ExamPaperQuestion examPaperQuestion);

    /**
     * 根据ID查询试卷题目关联
     */
    ExamPaperQuestion selectById(Integer id);

    /**
     * 根据试卷ID查询所有关联
     */
    List<ExamPaperQuestion> selectByExamPaperId(Integer examPaperId);
}