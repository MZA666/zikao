package com.example.mapper;

import com.example.entity.Major;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MajorMapper {
    /**
     * 插入专业
     */
    void insert(Major major);

    /**
     * 根据ID查询专业
     */
    Major selectById(@Param("id") Integer id);

    /**
     * 查询所有专业
     */
    List<Major> selectAll();

    /**
     * 根据名称查询专业
     */
    Major selectByName(@Param("name") String name);

    /**
     * 更新专业
     */
    void updateById(Major major);

    /**
     * 删除专业
     */
    void deleteById(@Param("id") Integer id);
}