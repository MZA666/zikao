package com.example.mapper;

import com.example.entity.File;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件数据操作接口
 */
public interface FileMapper {
    /**
     * 新增文件记录
     */
    int insert(File file);

    /**
     * 根据ID删除文件记录
     */
    int deleteById(Integer id);

    /**
     * 更新文件记录
     */
    int updateById(File file);

    /**
     * 根据ID查询文件记录
     */
    File selectById(Integer id);

    /**
     * 查询所有文件记录
     */
    List<File> selectAll(File file);

    /**
     * 根据上传者ID查询文件记录
     */
    List<File> selectByUploaderId(@Param("uploaderId") Integer uploaderId);

    /**
     * 根据状态查询文件记录
     */
    List<File> selectByStatus(@Param("status") Integer status);
    
    /**
     * 根据共享状态查询文件记录
     */
    List<File> selectBySharedStatus(@Param("isShared") Integer isShared);
}