package com.example.mapper.exam;

import com.example.entity.exam.ExamBankCollection;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExamBankCollectionMapper {
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
    List<ExamBankCollection> selectByUserId(@Param("userId") Integer userId);




    
    /**
     * 检查用户是否已收藏虚拟题库（基于学科ID和上传者ID）
     */
    ExamBankCollection selectByUserIdAndVirtualBankInfo(@Param("userId") Integer userId, 
                                                         @Param("subjectId") Integer subjectId, 
                                                         @Param("uploaderId") Integer uploaderId);
    
    /**
     * 检查用户是否已收藏虚拟题库（带bank_id为NULL条件）
     */
    ExamBankCollection selectByUserIdAndVirtualBankInfoWithNullBankId(@Param("userId") Integer userId, 
                                                         @Param("subjectId") Integer subjectId, 
                                                         @Param("uploaderId") Integer uploaderId);
    
    /**
     * 根据用户ID和虚拟题库信息删除收藏
     */
    int deleteByUserIdAndVirtualBankInfo(@Param("userId") Integer userId, 
                                         @Param("bankIdStr") String bankIdStr);
    
    /**
     * 根据用户ID和学科ID、上传者ID删除收藏
     */
    int deleteByUserIdAndSubjectUploader(@Param("userId") Integer userId,
                                         @Param("subjectId") Integer subjectId,
                                         @Param("uploaderId") Integer uploaderId);
}