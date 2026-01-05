package com.example.service.exam;

import com.example.entity.exam.ExamPaperQuestion;
import com.example.mapper.exam.ExamPaperQuestionMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 试卷题目关联服务实现类
 */
@Service
public class ExamPaperQuestionServiceImpl implements ExamPaperQuestionService {

    @Resource
    private ExamPaperQuestionMapper examPaperQuestionMapper;

    @Override
    public int insert(ExamPaperQuestion examPaperQuestion) {
        return examPaperQuestionMapper.insert(examPaperQuestion);
    }

    @Override
    public int deleteByExamPaperId(Integer examPaperId) {
        return examPaperQuestionMapper.deleteByExamPaperId(examPaperId);
    }

    @Override
    public int deleteByExamPaperIdAndQuestionId(Integer examPaperId, Integer questionId) {
        return examPaperQuestionMapper.deleteByExamPaperIdAndQuestionId(examPaperId, questionId);
    }

    @Override
    public int updateById(ExamPaperQuestion examPaperQuestion) {
        return examPaperQuestionMapper.updateById(examPaperQuestion);
    }

    @Override
    public ExamPaperQuestion selectById(Integer id) {
        return examPaperQuestionMapper.selectById(id);
    }

    @Override
    public List<ExamPaperQuestion> selectByExamPaperId(Integer examPaperId) {
        return examPaperQuestionMapper.selectByExamPaperId(examPaperId);
    }
}