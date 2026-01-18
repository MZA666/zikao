package com.example.service.exam;

import com.example.entity.exam.Subject;

import java.util.List;

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

    /**
     * 新增学科（简化方法）
     */
    void addSubject(Subject subject);

    /**
     * 根据ID查询学科
     */
    Subject findById(Integer id);

    /**
     * 查询所有学科
     */
    List<Subject> findAll();

    /**
     * 根据名称查询学科
     */
    Subject findByName(String name);

    /**
     * 根据专业ID查询学科
     */
    List<Subject> findByMajorId(Integer majorId);

    /**
     * 更新学科
     */
    void updateSubject(Subject subject);
}