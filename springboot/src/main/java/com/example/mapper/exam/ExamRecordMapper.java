package com.example.mapper.exam;

import com.example.entity.exam.ExamRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 考试记录数据操作接口
 */
public interface ExamRecordMapper {
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
    List<ExamRecord> selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据用户ID分页查询考试记录
     */
    List<ExamRecord> selectByUserIdWithPaging(@Param("userId") Integer userId, 
                                           @Param("offset") int offset, 
                                           @Param("limit") int limit);

    /**
     * 根据用户ID查询考试记录总数
     */
    int countByUserId(@Param("userId") Integer userId);

    /**
     * 根据学科ID查询考试记录
     */
    List<ExamRecord> selectBySubjectId(@Param("subjectId") Integer subjectId);

    /**
     * 根据日期范围查询考试记录
     */
    List<ExamRecord> selectByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);
}