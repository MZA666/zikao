package com.example.mapper.exam;

import com.example.entity.exam.ExamPaper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 考试试卷数据操作接口
 */
public interface ExamPaperMapper {
    /**
     * 新增试卷
     */
    int insert(ExamPaper examPaper);

    /**
     * 根据ID删除试卷
     */
    int deleteById(Integer id);

    /**
     * 更新试卷
     */
    int updateById(ExamPaper examPaper);

    /**
     * 根据ID查询试卷
     */
    ExamPaper selectById(Integer id);

    /**
     * 查询所有试卷
     */
    List<ExamPaper> selectAll(ExamPaper examPaper);

    /**
     * 根据学科ID查询试卷
     */
    List<ExamPaper> selectBySubjectId(@Param("subjectId") Integer subjectId);

    /**
     * 根据创建者ID查询试卷
     */
    List<ExamPaper> selectByCreatedBy(@Param("createdBy") Integer createdBy);
}