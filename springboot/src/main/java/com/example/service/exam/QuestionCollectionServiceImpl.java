package com.example.service.exam;

import com.example.entity.exam.QuestionCollection;
import com.example.mapper.exam.QuestionCollectionMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 题目收藏服务实现类
 */
@Service
public class QuestionCollectionServiceImpl implements QuestionCollectionService {

    @Resource
    private QuestionCollectionMapper collectionMapper;

    @Override
    public int insert(QuestionCollection questionCollection) {
        return collectionMapper.insert(questionCollection);
    }

    @Override
    public int deleteById(Integer id) {
        return collectionMapper.deleteById(id);
    }

    @Override
    public int deleteByUserIdAndQuestionId(Integer userId, Integer questionId) {
        return collectionMapper.deleteByUserIdAndQuestionId(userId, questionId);
    }

    @Override
    public int updateById(QuestionCollection questionCollection) {
        return collectionMapper.updateById(questionCollection);
    }

    @Override
    public QuestionCollection selectById(Integer id) {
        return collectionMapper.selectById(id);
    }

    @Override
    public List<QuestionCollection> selectByUserId(Integer userId) {
        return collectionMapper.selectByUserId(userId);
    }

    @Override
    public List<QuestionCollection> selectByQuestionId(Integer questionId) {
        return collectionMapper.selectByQuestionId(questionId);
    }

    @Override
    public QuestionCollection selectByUserIdAndQuestionId(Integer userId, Integer questionId) {
        return collectionMapper.selectByUserIdAndQuestionId(userId, questionId);
    }

    @Override
    public boolean isCollected(Integer userId, Integer questionId) {
        QuestionCollection collection = collectionMapper.selectByUserIdAndQuestionId(userId, questionId);
        return collection != null;
    }
}