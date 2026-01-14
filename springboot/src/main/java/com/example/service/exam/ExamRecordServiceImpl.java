package com.example.service.exam;

import com.example.entity.exam.ExamRecord;
import com.example.mapper.exam.ExamRecordMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 考试记录服务实现类
 */
@Service
public class ExamRecordServiceImpl implements ExamRecordService {

    @Resource
    private ExamRecordMapper examRecordMapper;

    @Override
    public int insert(ExamRecord examRecord) {
        System.out.println("Inserting exam record in service: " + examRecord);
        System.out.println("Answers in service: " + examRecord.getAnswers());
        if (examRecord.getAnswers() != null) {
            System.out.println("Answers size in service: " + examRecord.getAnswers().size());
        }
        return examRecordMapper.insert(examRecord);
    }

    @Override
    public int deleteById(Integer id) {
        return examRecordMapper.deleteById(id);
    }

    @Override
    public int updateById(ExamRecord examRecord) {
        return examRecordMapper.updateById(examRecord);
    }

    @Override
    public ExamRecord selectById(Integer id) {
        return examRecordMapper.selectById(id);
    }

    @Override
    public List<ExamRecord> selectAll(ExamRecord examRecord) {
        return examRecordMapper.selectAll(examRecord);
    }

    @Override
    public List<ExamRecord> selectByUserId(Integer userId) {
        return examRecordMapper.selectByUserId(userId);
    }

    @Override
    public List<ExamRecord> selectByUserIdWithPaging(Integer userId, int offset, int limit) {
        return examRecordMapper.selectByUserIdWithPaging(userId, offset, limit);
    }

    @Override
    public int countByUserId(Integer userId) {
        return examRecordMapper.countByUserId(userId);
    }

    @Override
    public PageInfo<ExamRecord> selectByUserIdWithPage(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ExamRecord> records = examRecordMapper.selectByUserId(userId);
        return PageInfo.of(records);
    }

    @Override
    public List<ExamRecord> selectBySubjectId(Integer subjectId) {
        return examRecordMapper.selectBySubjectId(subjectId);
    }

    @Override
    public List<ExamRecord> selectByDateRange(String startDate, String endDate) {
        return examRecordMapper.selectByDateRange(startDate, endDate);
    }
}