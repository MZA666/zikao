package com.example.service.exam;

import com.example.entity.exam.ExamRecord;

import java.util.List;

/**
 * 考试记录服务接口
 */
public interface ExamRecordService {
    /**
     * 新增考试记录
     */
    int insert(ExamRecord examRecord);

    /**
     * 根据ID删除考试记录
     */
    int deleteById(Integer id);

    /**
     * 更新考试记录
     */
    int updateById(ExamRecord examRecord);

    /**
     * 根据ID查询考试记录
     */
    ExamRecord selectById(Integer id);

    /**
     * 查询所有考试记录
     */
    List<ExamRecord> selectAll(ExamRecord examRecord);

    /**
     * 根据用户ID查询考试记录
     */
    List<ExamRecord> selectByUserId(Integer userId);

    /**
     * 根据学科ID查询考试记录
     */
    List<ExamRecord> selectBySubjectId(Integer subjectId);

    /**
     * 根据日期范围查询考试记录
     */
    List<ExamRecord> selectByDateRange(String startDate, String endDate);
}