package com.example.service.exam;

import com.example.entity.exam.Question;
import com.example.entity.exam.QuestionOption;
import com.example.mapper.exam.QuestionMapper;
import com.example.mapper.exam.QuestionOptionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Random;

/**
 * 题目服务实现类
 */
@Service
public class QuestionServiceImpl implements QuestionService {

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private QuestionOptionMapper optionMapper;

    @Override
    public int insert(Question question) {
        return questionMapper.insert(question);
    }

    @Override
    @Transactional
    public int deleteById(Integer id) {
        // 先删除相关的选项
        optionMapper.deleteByQuestionId(id);
        // 再删除题目
        return questionMapper.deleteById(id);
    }

    @Override
    public int updateById(Question question) {
        return questionMapper.updateById(question);
    }

    @Override
    public Question selectById(Integer id) {
        return questionMapper.selectById(id);
    }

    @Override
    public List<Question> selectAll(Question question) {
        return questionMapper.selectAll(question);
    }

    @Override
    public List<Question> selectBySubjectId(Integer subjectId) {
        return questionMapper.selectBySubjectId(subjectId);
    }

    @Override
    public List<Question> selectByType(String type) {
        return questionMapper.selectByType(type);
    }

    @Override
    public List<Question> selectByUserId(Integer userId) {
        return questionMapper.selectByUserId(userId);
    }

    @Override
    public Question selectQuestionWithOptions(Integer id) {
        Question question = questionMapper.selectById(id);
        if (question != null) {
            List<QuestionOption> options = optionMapper.selectByQuestionId(id);
            question.setOptions(options);
        }
        return question;
    }

    @Override
    @Transactional
    public int insertQuestionWithOptions(Question question, List<QuestionOption> options) {
        // 插入题目
        int result = questionMapper.insert(question);
        if (result > 0 && options != null && !options.isEmpty()) {
            // 插入选项
            for (QuestionOption option : options) {
                option.setQuestionId(question.getId());
                optionMapper.insert(option);
            }
        }
        return result;
    }

    @Override
    public List<Question> selectBySubjectIdAndType(Integer subjectId, String type) {
        Question question = new Question();
        question.setSubjectId(subjectId);
        question.setType(type);
        return questionMapper.selectAll(question);
    }

    @Override
    public List<Question> selectRandomQuestions(Integer subjectId, String type, Integer limit) {
        // 获取指定条件下的所有题目
        Question query = new Question();
        query.setSubjectId(subjectId);
        if (type != null && !type.isEmpty()) {
            query.setType(type);
        }
        List<Question> allQuestions = questionMapper.selectAll(query);
        
        // 随机选择指定数量的题目
        if (allQuestions.size() <= limit) {
            // 如果题目数量不足，直接返回所有题目
            return allQuestions;
        } else {
            // 随机选择limit个题目
            List<Question> result = new java.util.ArrayList<>();
            Random random = new Random();
            java.util.Collections.shuffle(allQuestions, random);
            for (int i = 0; i < Math.min(limit, allQuestions.size()); i++) {
                result.add(allQuestions.get(i));
            }
            return result;
        }
    }
}