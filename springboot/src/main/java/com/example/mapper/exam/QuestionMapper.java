package com.example.mapper.exam;

import com.example.entity.exam.Question;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目数据操作接口
 */
public interface QuestionMapper {
    /**
     * 新增题目
     */
    int insert(Question question);

    /**
     * 根据ID删除题目
     */
    int deleteById(Integer id);

    /**
     * 更新题目
     */
    int updateById(Question question);

    /**
     * 根据ID查询题目
     */
    Question selectById(Integer id);

    /**
     * 查询所有题目
     */
    List<Question> selectAll(Question question);

    /**
     * 根据学科ID查询题目
     */
    List<Question> selectBySubjectId(@Param("subjectId") Integer subjectId);

    /**
     * 根据题目类型查询题目
     */
    List<Question> selectByType(@Param("type") String type);

    /**
     * 根据用户ID查询收藏的题目
     */
    List<Question> selectByUserId(@Param("userId") Integer userId);
}