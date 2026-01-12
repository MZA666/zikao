package com.example.mapper.exam;

import com.example.entity.exam.Question;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QuestionMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(Question record);

    int insertSelective(Question record);

    Question selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Question record);

    int updateByPrimaryKey(Question record);

    /**
     * 新增
     */
    int insertOrUpdate(Question record);

    /**
     * 删除
     */
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(Question record);

    /**
     * 根据ID查询
     */
    Question selectById(Integer id);

    /**
     * 查询所有
     */
    List<Question> selectAll(Question question);

    /**
     * 根据学科ID查询
     */
    List<Question> selectBySubjectId(@Param("subjectId") Integer subjectId);

    /**
     * 根据题目类型查询
     */
    List<Question> selectByType(@Param("type") String type);

    /**
     * 随机获取指定数量的题目
     */
    List<Question> selectRandomQuestions(@Param("subjectId") Integer subjectId, 
                                       @Param("type") String type, 
                                       @Param("limit") Integer limit);

    /**
     * 根据难度随机获取指定数量的题目
     */
    List<Question> selectRandomQuestionsByDifficulty(@Param("subjectId") Integer subjectId, 
                                                   @Param("difficulty") String difficulty, 
                                                   @Param("limit") Integer limit);

    /**
     * 根据类型和难度随机获取指定数量的题目
     */
    List<Question> selectRandomQuestionsByTypeAndDifficulty(@Param("subjectId") Integer subjectId, 
                                                          @Param("type") String type, 
                                                          @Param("difficulty") String difficulty, 
                                                          @Param("limit") Integer limit);
}