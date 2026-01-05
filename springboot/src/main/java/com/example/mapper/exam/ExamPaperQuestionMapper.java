package com.example.mapper.exam;

import com.example.entity.exam.ExamPaperQuestion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 试卷题目关联数据操作接口
 */
public interface ExamPaperQuestionMapper {
    /**
     * 新增试卷题目关联
     */
    int insert(ExamPaperQuestion examPaperQuestion);

    /**
     * 根据试卷ID删除关联
     */
    int deleteByExamPaperId(@Param("examPaperId") Integer examPaperId);

    /**
     * 根据试卷ID和题目ID删除关联
     */
    int deleteByExamPaperIdAndQuestionId(@Param("examPaperId") Integer examPaperId, @Param("questionId") Integer questionId);

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
    List<ExamPaperQuestion> selectByExamPaperId(@Param("examPaperId") Integer examPaperId);
}