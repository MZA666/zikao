package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.entity.File;
import com.example.enums.FileStatusEnum;
import com.example.exception.CustomException;
import com.example.mapper.FileMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 文件业务处理
 */
@Service
public class FileService {

    @Resource
    private FileMapper fileMapper;

    /**
     * 上传文件
     */
    public File uploadFile(MultipartFile multipartFile, Integer uploaderId, String uploader, String description, Integer isShared) {
        // 验证文件
        validateFile(multipartFile);

        // 生成文件名
        String originalName = multipartFile.getOriginalFilename();
        String fileName = System.currentTimeMillis() + "_" + uploaderId + "_" + originalName;
        String fileType = getFileType(originalName);
        Long fileSize = multipartFile.getSize();

        // 创建文件记录
        File file = new File();
        file.setOriginalName(originalName);
        file.setFileName(fileName);
        file.setFilePath(System.getProperty("user.dir") + "/files/" + fileName);
        file.setFileSize(fileSize);
        file.setFileType(fileType);
        file.setUploader(uploader);
        file.setUploaderId(uploaderId);
        file.setStatus(isShared == 1 ? FileStatusEnum.PENDING.getCode() : FileStatusEnum.APPROVED.getCode()); // 共享文件需要审核，私密文件直接通过
        file.setUploadTime(new Date());
        file.setDescription(description);
        file.setIsShared(isShared); // 设置共享状态

        // 保存到数据库
        fileMapper.insert(file);

        // 保存文件到磁盘
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(file.getFilePath());
            java.nio.file.Files.createDirectories(path.getParent());
            multipartFile.transferTo(path.toFile());
        } catch (IOException e) {
            throw new CustomException("文件保存失败");
        }
        
        return file; // 返回文件对象
    }

    /**
     * 审核文件
     */
    public void auditFile(Integer fileId, Integer auditorId, Integer status, String reason) {
        File file = fileMapper.selectById(fileId);
        if (ObjectUtil.isNull(file)) {
            throw new CustomException("文件不存在");
        }

        // 设置审核信息
        file.setAuditorId(auditorId);
        file.setStatus(status);
        file.setReason(reason);
        file.setAuditTime(new Date());

        fileMapper.updateById(file);
    }

    /**
     * 根据上传者ID查询文件（支持文件名模糊查询）
     */
    public List<File> selectByUploaderId(Integer uploaderId, String fileName) {
        File file = new File();
        file.setUploaderId(uploaderId);
        if (fileName != null && !fileName.trim().isEmpty()) {
            file.setOriginalName(fileName); // 在实体类中设置文件名，用于模糊查询
        }
        return fileMapper.selectAll(file);
    }

    /**
     * 根据上传者ID查询文件（兼容旧方法）
     */
    public List<File> selectByUploaderId(Integer uploaderId) {
        return selectByUploaderId(uploaderId, null);
    }

    /**
     * 根据状态查询文件
     */
    public List<File> selectByStatus(Integer status) {
        return fileMapper.selectByStatus(status);
    }
    
    /**
     * 根据共享状态查询文件
     */
    public List<File> selectBySharedStatus(Integer isShared) {
        return fileMapper.selectBySharedStatus(isShared);
    }
    
    /**
     * 查询已通过审核的共享文件（支持文件名模糊查询）
     */
    public List<File> selectApprovedSharedFiles(String fileName) {
        File file = new File();
        file.setIsShared(1); // 共享文件
        file.setStatus(FileStatusEnum.APPROVED.getCode()); // 已通过审核
        if (fileName != null && !fileName.trim().isEmpty()) {
            file.setOriginalName(fileName); // 在实体类中设置文件名，用于模糊查询
        }
        return fileMapper.selectAll(file);
    }
    
    /**
     * 查询已通过审核的共享文件（兼容旧方法）
     */
    public List<File> selectApprovedSharedFiles() {
        return selectApprovedSharedFiles(null);
    }

    /**
     * 验证上传的文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException("请选择文件");
        }

        // 检查文件大小 (限制为50MB)
        long maxSize = 50 * 1024 * 1024; // 50MB
        if (file.getSize() > maxSize) {
            throw new CustomException("文件大小不能超过50MB");
        }

        // 检查文件类型
        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new CustomException("文件名不能为空");
        }

        // 允许的文件类型
        String[] allowedTypes = {
            "application/pdf", // PDF
            "application/msword", // DOC
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // DOCX
            "application/vnd.ms-powerpoint", // PPT
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", // PPTX
            "application/vnd.ms-excel", // XLS
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // XLSX
            "text/plain", // TXT
            "image/jpeg", // JPG
            "image/png" // PNG
        };

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new CustomException("无法识别文件类型");
        }

        boolean isAllowed = false;
        for (String allowedType : allowedTypes) {
            if (contentType.toLowerCase().contains(allowedType.split("/")[1])) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new CustomException("不支持的文件类型: " + contentType);
        }
    }

    /**
     * 获取文件类型
     */
    private String getFileType(String originalName) {
        if (originalName == null) {
            return "";
        }
        int lastDot = originalName.lastIndexOf(".");
        if (lastDot > 0) {
            return originalName.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * 根据ID查询文件
     */
    public File selectById(Integer fileId) {
        return fileMapper.selectById(fileId);
    }
    
    /**
     * 根据ID删除文件
     */
    public void deleteById(Integer fileId) {
        fileMapper.deleteById(fileId);
    }
}