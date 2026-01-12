package com.example.service.exam;

import com.example.entity.exam.ExamBank;
import com.example.mapper.exam.ExamBankMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class ExamBankServiceImpl implements ExamBankService {

    @Resource
    private ExamBankMapper examBankMapper;

    @Override
    public int insert(ExamBank examBank) {
        return examBankMapper.insert(examBank);
    }

    @Override
    public int deleteById(Integer bankId) {
        return examBankMapper.deleteById(bankId);
    }

    @Override
    public int updateById(ExamBank examBank) {
        return examBankMapper.updateById(examBank);
    }

    @Override
    public ExamBank selectById(Integer bankId) {
        return examBankMapper.selectById(bankId);
    }

    @Override
    public List<ExamBank> selectAll(ExamBank examBank) {
        return examBankMapper.selectAll(examBank);
    }

    @Override
    public List<ExamBank> selectBySubject(String subject) {
        return examBankMapper.selectBySubject(subject);
    }

    @Override
    public List<ExamBank> selectByUploaderId(Integer uploaderId) {
        return examBankMapper.selectByUploaderId(uploaderId);
    }

    @Override
    public List<ExamBank> selectByStatus(Integer status) {
        return examBankMapper.selectByStatus(status);
    }
}