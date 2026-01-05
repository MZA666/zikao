package com.example.service.exam;

import com.example.entity.exam.ExamBank;

import java.util.List;

/**
 * 题库服务接口
 */
public interface ExamBankService {
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
     * 根据学科查询题库
     */
    List<ExamBank> selectBySubject(String subject);

    /**
     * 根据上传者ID查询题库
     */
    List<ExamBank> selectByUploaderId(Integer uploaderId);

    /**
     * 根据状态查询题库
     */
    List<ExamBank> selectByStatus(Integer status);
}