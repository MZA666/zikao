package com.example.mapper.exam;

import com.example.entity.exam.ExamBankCollection;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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
     * 根据用户ID和题库ID查询收藏
     */
    ExamBankCollection selectByUserIdAndBankId(@Param("userId") Integer userId,
                                               @Param("bankId") Integer bankId);
    
    /**
     * 根据用户ID和题库ID删除收藏
     */
    int deleteByUserIdAndBankId(@Param("userId") Integer userId,
                                @Param("bankId") Integer bankId);
    
    /**
     * 查询用户收藏的题库及其专业和学科信息
     */
    List<Map<String, Object>> selectUserBanksWithMajorSubject(@Param("userId") Integer userId);
}