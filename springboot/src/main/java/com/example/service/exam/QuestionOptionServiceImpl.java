package com.example.service.exam;

import com.example.entity.exam.QuestionOption;
import com.example.mapper.exam.QuestionOptionMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 题目选项服务实现类
 */
@Service
public class QuestionOptionServiceImpl implements QuestionOptionService {

    @Resource
    private QuestionOptionMapper optionMapper;

    @Override
    public int insert(QuestionOption questionOption) {
        return optionMapper.insert(questionOption);
    }

    @Override
    public int deleteByQuestionId(Integer questionId) {
        return optionMapper.deleteByQuestionId(questionId);
    }

    @Override
    public int updateById(QuestionOption questionOption) {
        return optionMapper.updateById(questionOption);
    }

    @Override
    public QuestionOption selectById(Integer id) {
        return optionMapper.selectById(id);
    }

    @Override
    public List<QuestionOption> selectByQuestionId(Integer questionId) {
        return optionMapper.selectByQuestionId(questionId);
    }
}