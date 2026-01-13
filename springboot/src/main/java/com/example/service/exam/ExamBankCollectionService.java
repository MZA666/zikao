package com.example.service.exam;

import com.example.entity.exam.ExamBankCollection;

import java.util.List;

public interface ExamBankCollectionService {
    /**
     * 新增题库收藏
     */
    int insert(ExamBankCollection collection);

    /**
     * 根据ID删除题库收藏
     */
    int deleteById(Integer id);



    /**
     * 更新题库收藏
     */
    int updateById(ExamBankCollection collection);

    /**
     * 根据ID查询题库收藏
     */
    ExamBankCollection selectById(Integer id);

    /**
     * 根据用户ID查询收藏的题库
     */
    List<ExamBankCollection> selectByUserId(Integer userId);






    
    /**
     * 判断用户是否已收藏虚拟题库（基于学科ID和上传者ID）
     */
    boolean isCollectedForVirtualBank(Integer userId, Integer subjectId, Integer uploaderId);
    
    /**
     * 根据用户ID和银行ID（扩展版）删除收藏
     */
    int deleteByUserIdAndBankIdExtended(Integer userId, Integer bankId, String bankIdStr);
    
    /**
     * 根据用户ID和学科ID、上传者ID删除收藏（用于虚拟题库）
     */
    int deleteByUserIdAndSubjectUploader(Integer userId, Integer subjectId, Integer uploaderId);
}