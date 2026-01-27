package com.example.service.exam;

import com.example.entity.exam.ExamBankCollection;
import com.example.mapper.exam.ExamBankCollectionMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class ExamBankCollectionServiceImpl implements ExamBankCollectionService {

    @Resource
    private ExamBankCollectionMapper collectionMapper;

    @Override
    public int insert(ExamBankCollection collection) {
        System.out.println("Debug: Service insert - userId:" + collection.getUserId() + ", bankId:" + collection.getBankId() + ", bankName:" + collection.getBankName());
        return collectionMapper.insert(collection);
    }

    @Override
    public int deleteById(Integer id) {
        return collectionMapper.deleteById(id);
    }

    @Override
    public int updateById(ExamBankCollection collection) {
        return collectionMapper.updateById(collection);
    }

    @Override
    public ExamBankCollection selectById(Integer id) {
        return collectionMapper.selectById(id);
    }

    @Override
    public List<ExamBankCollection> selectByUserId(Integer userId) {
        return collectionMapper.selectByUserId(userId);
    }

    @Override
    public ExamBankCollection selectByUserIdAndBankId(Integer userId, Integer bankId) {
        return collectionMapper.selectByUserIdAndBankId(userId, bankId);
    }
    
    @Override
    public int deleteByUserIdAndBankId(Integer userId, Integer bankId) {
        return collectionMapper.deleteByUserIdAndBankId(userId, bankId);
    }
    
    @Override
    public List<Map<String, Object>> selectUserBanksWithMajorSubject(Integer userId) {
        return collectionMapper.selectUserBanksWithMajorSubject(userId);
    }
}