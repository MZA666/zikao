package com.example.service.exam;

import com.example.entity.exam.ExamPaper;
import com.example.mapper.exam.ExamPaperMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 考试试卷服务实现类
 */
@Service
public class ExamPaperServiceImpl implements ExamPaperService {

    @Resource
    private ExamPaperMapper examPaperMapper;

    @Override
    public int insert(ExamPaper examPaper) {
        return examPaperMapper.insert(examPaper);
    }

    @Override
    public int deleteById(Integer id) {
        return examPaperMapper.deleteById(id);
    }

    @Override
    public int updateById(ExamPaper examPaper) {
        return examPaperMapper.updateById(examPaper);
    }

    @Override
    public ExamPaper selectById(Integer id) {
        return examPaperMapper.selectById(id);
    }

    @Override
    public List<ExamPaper> selectAll(ExamPaper examPaper) {
        return examPaperMapper.selectAll(examPaper);
    }

    @Override
    public List<ExamPaper> selectBySubjectId(Integer subjectId) {
        return examPaperMapper.selectBySubjectId(subjectId);
    }

    @Override
    public List<ExamPaper> selectByCreatedBy(Integer createdBy) {
        return examPaperMapper.selectByCreatedBy(createdBy);
    }
}