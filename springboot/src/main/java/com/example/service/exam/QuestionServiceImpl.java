package com.example.service.exam;

import com.example.entity.exam.Question;
import com.example.entity.exam.QuestionOption;
import com.example.mapper.exam.QuestionMapper;
import com.example.mapper.exam.QuestionOptionMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Random;

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
    public int deleteById(Integer id) {
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
    public List<Question> selectRandomQuestions(Integer subjectId, String type, Integer limit) {
        return questionMapper.selectRandomQuestions(subjectId, type, limit);
    }

    @Override
    public List<Question> selectRandomQuestionsByDifficulty(Integer subjectId, String difficulty, Integer limit) {
        return questionMapper.selectRandomQuestionsByDifficulty(subjectId, difficulty, limit);
    }

    @Override
    public List<Question> selectRandomQuestionsByTypeAndDifficulty(Integer subjectId, String type, String difficulty, Integer limit) {
        return questionMapper.selectRandomQuestionsByTypeAndDifficulty(subjectId, type, difficulty, limit);
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
    public List<Question> selectByIds(List<Integer> ids) {
        return questionMapper.selectByIds(ids);
    }

    /**
     * 插入题目及其选项
     */
    public int insertQuestionWithOptions(Question question, List<QuestionOption> options) {
        // 先插入题目
        int result = questionMapper.insert(question);
        if (result > 0 && options != null && !options.isEmpty()) {
            // 获取刚插入的题目ID
            Integer questionId = question.getId();
            // 插入选项
            for (QuestionOption option : options) {
                option.setQuestionId(questionId);
                optionMapper.insert(option);
            }
        }
        return result;
    }
}