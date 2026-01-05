package com.example.mapper.exam;

import com.example.entity.exam.Subject;

import java.util.List;

/**
 * 学科数据操作接口
 */
public interface SubjectMapper {
    /**
     * 新增学科
     */
    int insert(Subject subject);

    /**
     * 根据ID删除学科
     */
    int deleteById(Integer id);

    /**
     * 更新学科
     */
    int updateById(Subject subject);

    /**
     * 根据ID查询学科
     */
    Subject selectById(Integer id);

    /**
     * 查询所有学科
     */
    List<Subject> selectAll(Subject subject);
}