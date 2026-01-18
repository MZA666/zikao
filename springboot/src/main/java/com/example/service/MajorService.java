package com.example.service;

import com.example.entity.Major;
import com.example.mapper.MajorMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class MajorService {

    @Resource
    private MajorMapper majorMapper;

    /**
     * 新增专业
     */
    public void addMajor(Major major) {
        majorMapper.insert(major);
    }

    /**
     * 根据ID查询专业
     */
    public Major findById(Integer id) {
        return majorMapper.selectById(id);
    }

    /**
     * 查询所有专业
     */
    public List<Major> findAll() {
        return majorMapper.selectAll();
    }

    /**
     * 根据名称查询专业
     */
    public Major findByName(String name) {
        return majorMapper.selectByName(name);
    }

    /**
     * 更新专业
     */
    public void updateMajor(Major major) {
        majorMapper.updateById(major);
    }

    /**
     * 删除专业
     */
    public void deleteById(Integer id) {
        majorMapper.deleteById(id);
    }
}