package com.example.service.exam;

import com.example.entity.exam.ExamBankCollection;
import com.example.mapper.exam.ExamBankCollectionMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class ExamBankCollectionServiceImpl implements ExamBankCollectionService {

    @Resource
    private ExamBankCollectionMapper collectionMapper;

    @Override
    public int insert(ExamBankCollection collection) {
        System.out.println("Debug: Service insert - userId:" + collection.getUserId() + ", subjectId:" + collection.getSubjectId() + ", uploaderId:" + collection.getUploaderId());
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
    public boolean isCollectedForVirtualBank(Integer userId, Integer subjectId, Integer uploaderId) {
        ExamBankCollection collection = collectionMapper.selectByUserIdAndVirtualBankInfoWithNullBankId(userId, subjectId, uploaderId);
        return collection != null;
    }

    @Override
    public int deleteByUserIdAndBankIdExtended(Integer userId, Integer bankId, String bankIdStr) {
        if (bankId != null) {
            // 删除真实的题库收藏 - 现在使用扩展方法处理
            // 对于真实题库，通过虚拟信息方式删除（使用bankIdStr）
            return collectionMapper.deleteByUserIdAndVirtualBankInfo(userId, bankIdStr);
        } else {
            // 删除虚拟题库收藏（基于学科ID和上传者ID）
            return collectionMapper.deleteByUserIdAndVirtualBankInfo(userId, bankIdStr);
        }
    }

    @Override
    public int deleteByUserIdAndSubjectUploader(Integer userId, Integer subjectId, Integer uploaderId) {
        return collectionMapper.deleteByUserIdAndSubjectUploader(userId, subjectId, uploaderId);
    }
}