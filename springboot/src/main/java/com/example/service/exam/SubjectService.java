package com.example.service.exam;

import com.example.entity.exam.Subject;

import java.util.List;

/**
 * 学科服务接口
 */
public interface SubjectService {
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