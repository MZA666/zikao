package com.example.mapper.exam;

import com.example.entity.exam.ExamBank;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题库数据操作接口
 */
public interface ExamBankMapper {
    /**
     * 新增题库
     */
    int insert(ExamBank examBank);

    /**
     * 根据ID删除题库
     */
    int deleteById(Integer bankId);

    /**
     * 更新题库
     */
    int updateById(ExamBank examBank);

    /**
     * 根据ID查询题库
     */
    ExamBank selectById(Integer bankId);

    /**
     * 查询所有题库
     */
    List<ExamBank> selectAll(ExamBank examBank);
    
    /**
     * 查询题库总数
     */
    int selectCount(ExamBank examBank);
    
    /**
     * 分页查询题库
     */
    List<ExamBank> selectAllWithPaging(@Param("examBank") ExamBank examBank, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 根据学科查询题库
     */
    List<ExamBank> selectBySubject(@Param("subject") String subject);

    /**
     * 根据上传者ID查询题库
     */
    List<ExamBank> selectByUploaderId(@Param("uploaderId") Integer uploaderId);

    /**
     * 根据状态查询题库
     */
    List<ExamBank> selectByStatus(@Param("status") Integer status);
}