package com.example.service.exam;

import com.example.entity.exam.Subject;
import com.example.mapper.exam.SubjectMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 学科服务实现类
 */
@Service
public class SubjectServiceImpl implements SubjectService {

    @Resource
    private SubjectMapper subjectMapper;

    @Override
    public int insert(Subject subject) {
        return subjectMapper.insert(subject);
    }

    @Override
    public int deleteById(Integer id) {
        return subjectMapper.deleteById(id);
    }

    @Override
    public int updateById(Subject subject) {
        return subjectMapper.updateById(subject);
    }

    @Override
    public Subject selectById(Integer id) {
        return subjectMapper.selectById(id);
    }

    @Override
    public List<Subject> selectAll(Subject subject) {
        return subjectMapper.selectAll(subject);
    }

    @Override
    public void addSubject(Subject subject) {
        subjectMapper.insert(subject);
    }

    @Override
    public Subject findById(Integer id) {
        return subjectMapper.selectById(id);
    }

    @Override
    public List<Subject> findAll() {
        return subjectMapper.selectAll(null);
    }

    @Override
    public Subject findByName(String name) {
        // 由于mapper中没有直接按名称查询的方法，我们先获取全部再过滤
        List<Subject> subjects = subjectMapper.selectAll(null);
        for (Subject subject : subjects) {
            if (subject.getName().equals(name)) {
                return subject;
            }
        }
        return null;
    }

    @Override
    public List<Subject> findByMajorId(Integer majorId) {
        // 由于mapper中没有直接按专业ID查询的方法，我们先获取全部再过滤
        List<Subject> subjects = subjectMapper.selectAll(null);
        return subjects.stream()
                .filter(subject -> subject.getMajorId() != null && subject.getMajorId().equals(majorId))
                .toList();
    }

    @Override
    public void updateSubject(Subject subject) {
        subjectMapper.updateById(subject);
    }
}