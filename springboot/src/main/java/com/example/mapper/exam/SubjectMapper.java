package com.example.mapper.exam;

import com.example.entity.exam.Subject;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SubjectMapper {
    /**
     * 新增学科
     */
    int insert(Subject subject);

    /**
     * 根据ID删除学科
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 更新学科
     */
    int updateById(Subject subject);

    /**
     * 根据ID查询学科
     */
    Subject selectById(@Param("id") Integer id);

    /**
     * 查询所有学科
     */
    List<Subject> selectAll(Subject subject);

    /**
     * 根据名称查询学科
     */
    Subject selectByName(@Param("name") String name);

    /**
     * 根据专业ID查询学科
     */
    List<Subject> selectByMajorId(@Param("majorId") Integer majorId);
}