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
}