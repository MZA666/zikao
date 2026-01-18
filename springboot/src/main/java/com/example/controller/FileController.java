package com.example.controller;

import com.example.common.Result;
import com.example.entity.File;
import com.example.entity.Major;
import com.example.entity.exam.Subject;
import com.example.enums.FileStatusEnum;
import com.example.service.FileService;
import com.example.service.MajorService;
import com.example.service.exam.SubjectService;
import com.example.utils.FileUtil;
import com.example.utils.WordToHtmlUtil;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    // 表示本地磁盘文件的存储路径
    private static final String filePath = System.getProperty("user.dir") + "/files/";

    @Value("${fileBaseUrl}")
    private String fileBaseUrl;

    @Value("${server.port}")
    private String port;

    private final FileService fileService;
    private final MajorService majorService;
    private final SubjectService subjectService;
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    public FileController(FileService fileService, MajorService majorService, SubjectService subjectService) {
        this.fileService = fileService;
        this.majorService = majorService;
        this.subjectService = subjectService;
    }

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result upload(@RequestParam MultipartFile file, 
                         @RequestParam Integer uploaderId, 
                         @RequestParam String uploader,
                         @RequestParam(required = false) String description,
                         @RequestParam(defaultValue = "0") Integer isShared,
                         @RequestParam(required = false) Integer majorId,
                         @RequestParam(required = false) Integer disciplineId,
                         @RequestParam(required = false) String majorName,
                         @RequestParam(required = false) String disciplineName) {
        try {
            // 如果专业或学科不存在，但提供了名称，则尝试创建
            Integer finalMajorId = majorId;
            Integer finalDisciplineId = disciplineId;
            
            // 如果提供了专业名称但没有提供ID，则尝试创建专业
            if (majorName != null && !majorName.trim().isEmpty() && (majorId == null || majorId <= 0)) {
                // 检查专业是否已存在
                com.example.entity.Major existingMajor = majorService.findByName(majorName);
                if (existingMajor != null) {
                    finalMajorId = existingMajor.getId(); // 使用已存在的专业ID
                } else {
                    // 创建新的专业
                    com.example.entity.Major newMajor = new com.example.entity.Major();
                    newMajor.setName(majorName);
                    majorService.addMajor(newMajor);
                    finalMajorId = newMajor.getId(); // 使用新创建的专业ID
                }
            }
            
            // 如果提供了学科名称但没有提供ID，则尝试创建学科
            if (disciplineName != null && !disciplineName.trim().isEmpty() && (disciplineId == null || disciplineId <= 0)) {
                // 检查学科是否已存在
                com.example.entity.exam.Subject existingSubject = subjectService.findByName(disciplineName);
                if (existingSubject != null) {
                    finalDisciplineId = existingSubject.getId(); // 使用已存在的学科ID
                } else {
                    // 创建新的学科
                    com.example.entity.exam.Subject newSubject = new com.example.entity.exam.Subject();
                    newSubject.setName(disciplineName);
                    // 如果有专业ID，设置关联关系
                    if (finalMajorId != null) {
                        newSubject.setMajorId(finalMajorId);
                    }
                    subjectService.addSubject(newSubject);
                    finalDisciplineId = newSubject.getId(); // 使用新创建的学科ID
                }
            }
            
            File uploadedFile = fileService.uploadFile(file, uploaderId, uploader, description, isShared, finalMajorId, finalDisciplineId);
            return Result.success(uploadedFile);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 文件下载
     */
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        // 设置下载文件http响应头
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        // 拼接完整的文件存储路径
        String realFilePath = filePath + fileName;
        try {
            // 通过文件的存储路径拿到文件字节数组
            byte[] bytes = FileUtil.readBytes(realFilePath);
            ServletOutputStream os = response.getOutputStream();
            // 将文件字节数组写出到文件流
            os.write(bytes);
            os.flush();
            os.close();
        } catch (IOException e) {
            System.out.println("文件下载错误");
        }
    }
    
    /**
     * 文件预览
     */
    @GetMapping("/preview/{fileName}")
    public void preview(@PathVariable String fileName, HttpServletResponse response) {
        // 设置预览文件http响应头，允许浏览器预览
        String mimeType = getMimeType(fileName);
        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", "inline;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        // 拼接完整的文件存储路径
        String realFilePath = filePath + fileName;
        try {
            // 通过文件的存储路径拿到文件字节数组
            byte[] bytes = FileUtil.readBytes(realFilePath);
            ServletOutputStream os = response.getOutputStream();
            // 将文件字节数组写出到文件流
            os.write(bytes);
            os.flush();
            os.close();
        } catch (IOException e) {
            System.out.println("文件预览错误");
        }
    }

    /**
     * 根据文件扩展名获取MIME类型
     */
    private String getMimeType(String fileName) {
        String extension = "";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        }

        switch (extension) {
            case "pdf":
                return "application/pdf";

            case "txt":
                return "text/plain";


            default:
                return "application/octet-stream"; // 默认类型
        }
    }

    /**
     * 学员查询自己上传的文件（支持专业和学科筛选）
     */
    @GetMapping("/myFiles")
    public Result getMyFiles(@RequestParam Integer uploaderId, 
                             @RequestParam(required = false) String fileName,
                             @RequestParam(required = false) Integer majorId,
                             @RequestParam(required = false) Integer disciplineId) {
        try {
            List<File> files = fileService.selectByUploaderId(uploaderId, fileName, majorId, disciplineId);
            return Result.success(files);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 学员查询自己上传的文件（分页）
     */
    @GetMapping("/myFiles/page")
    public Result getMyFilesPage(@RequestParam Integer uploaderId,
                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "20") Integer pageSize,
                                 @RequestParam(required = false) String fileName,
                                 @RequestParam(required = false) Integer majorId,
                                 @RequestParam(required = false) Integer disciplineId) {
        try {
            PageInfo<File> pageInfo = fileService.selectByUploaderIdWithPage(uploaderId, pageNum, pageSize, fileName, majorId, disciplineId);
            return Result.success(pageInfo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询已通过审核的共享文件（支持专业和学科筛选）
     */
    @GetMapping("/sharedFiles")
    public Result getSharedFiles(@RequestParam Integer status, 
                                 @RequestParam(required = false) String fileName,
                                 @RequestParam(required = false) Integer majorId,
                                 @RequestParam(required = false) Integer disciplineId) {
        try {
            if (status == FileStatusEnum.APPROVED.getCode()) {
                List<File> files = fileService.selectApprovedSharedFiles(fileName, majorId, disciplineId);
                return Result.success(files);
            } else {
                return Result.error("参数错误");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询已通过审核的共享文件（分页）
     */
    @GetMapping("/sharedFiles/page")
    public Result getSharedFilesPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "20") Integer pageSize,
                                     @RequestParam(required = false) String fileName,
                                     @RequestParam(required = false) Integer majorId,
                                     @RequestParam(required = false) Integer disciplineId) {
        try {
            PageInfo<File> pageInfo = fileService.selectApprovedSharedFilesWithPage(pageNum, pageSize, fileName, majorId, disciplineId);
            return Result.success(pageInfo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 管理员查询待审核文件
     */
    @GetMapping("/pendingFiles")
    public Result getPendingFiles() {
        try {
            List<File> files = fileService.selectByStatus(FileStatusEnum.PENDING.getCode());
            return Result.success(files);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 管理员查询待审核文件（分页）
     */
    @GetMapping("/pendingFiles/page")
    public Result getPendingFilesPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            PageInfo<File> pageInfo = fileService.selectByStatusWithPage(FileStatusEnum.PENDING.getCode(), pageNum, pageSize);
            return Result.success(pageInfo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 管理员审核文件
     */
    @PutMapping("/audit")
    public Result auditFile(@RequestParam Integer fileId,
                            @RequestParam Integer auditorId,
                            @RequestParam Integer status,
                            @RequestParam(required = false) String reason) {
        try {
            fileService.auditFile(fileId, auditorId, status, reason);
            return Result.success("审核成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据状态查询文件（所有状态）
     */
    @GetMapping("/filesByStatus")
    public Result getFilesByStatus(@RequestParam Integer status) {
        try {
            List<File> files = fileService.selectByStatus(status);
            return Result.success(files);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据状态查询文件（分页）
     */
    @GetMapping("/filesByStatus/page")
    public Result getFilesByStatusPage(@RequestParam Integer status,
                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            PageInfo<File> pageInfo = fileService.selectByStatusWithPage(status, pageNum, pageSize);
            return Result.success(pageInfo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据文件ID查询文件信息
     */
    @GetMapping("/{fileId}")
    public Result getFileById(@PathVariable Integer fileId) {
        try {
            File file = fileService.selectById(fileId);
            if (file != null) {
                return Result.success(file);
            } else {
                return Result.error("文件不存在");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取Word文档的HTML内容
     */
    @GetMapping("/word-to-html/{fileId}")
    public Result getWordToHtml(@PathVariable Integer fileId) {
        try {
            File file = fileService.selectById(fileId);
            if (file == null) {
                return Result.error("文件不存在");
            }
            
            // 检查文件类型是否为Word文档 - 使用更宽松的检查
            String fileType = file.getFileType().toLowerCase();
            String originalName = file.getOriginalName().toLowerCase();
            
            boolean isWordDocument = isWordFileType(fileType) || isWordFileName(originalName);
            
            if (!isWordDocument) {
                return Result.error("文件不是Word文档");
            }
            
            // 拼接完整的文件存储路径
            String realFilePath = filePath + file.getFileName();
            
            try {
                // 使用POI将Word文档转换为HTML
                String htmlContent = WordToHtmlUtil.convertWordToHtml(realFilePath);
                return Result.success(htmlContent);
            } catch (Exception e) {
                System.err.println("Word文档转换失败: " + e.getMessage());
                e.printStackTrace();
                return Result.error("转换Word文档失败: " + e.getMessage());
            }
        } catch (Exception e) {
            return Result.error("获取Word文档失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    public Result deleteFile(@PathVariable Integer fileId, @RequestParam Integer uploaderId) {
        try {
            File file = fileService.selectById(fileId);
            if (file == null) {
                return Result.error("文件不存在");
            }
            
            // 检查文件是否属于当前用户
            if (!file.getUploaderId().equals(uploaderId)) {
                return Result.error("无权删除此文件");
            }
            
            // 删除数据库记录
            fileService.deleteById(fileId);
            
            // 删除物理文件
            String realFilePath = filePath + file.getFileName();
            java.io.File physicalFile = new java.io.File(realFilePath);
            if (physicalFile.exists()) {
                physicalFile.delete();
            }
            
            return Result.success("文件删除成功");
        } catch (Exception e) {
            return Result.error("删除文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 判断文件扩展名是否为Word文档类型
     */
    private boolean isWordFileType(String fileType) {
        return "doc".equals(fileType) || "docx".equals(fileType);
    }
    
    /**
     * 判断文件名是否为Word文档
     */
    private boolean isWordFileName(String originalName) {
        return originalName.endsWith(".doc") || originalName.endsWith(".docx");
    }
    
    /**
     * 获取所有专业列表
     */
    @GetMapping("/majors")
    public Result getAllMajors() {
        try {
            List<Major> majors = majorService.findAll();
            return Result.success(majors);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取所有学科列表
     */
    @GetMapping("/disciplines")
    public Result getAllDisciplines() {
        try {
            List<Subject> subjects = subjectService.findAll();
            return Result.success(subjects);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据专业ID获取学科列表
     */
    @GetMapping("/disciplines/by-major/{majorId}")
    public Result getDisciplinesByMajor(@PathVariable Integer majorId) {
        try {
            List<Subject> subjects = subjectService.findByMajorId(majorId);
            return Result.success(subjects);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建专业
     */
    @PostMapping("/major")
    public Result createMajor(@RequestBody Major major) {
        try {
            logger.info("创建专业请求，专业名称: {}", major.getName());
            // 检查专业是否已存在
            Major existingMajor = majorService.findByName(major.getName());
            if (existingMajor != null) {
                logger.warn("专业已存在，专业名称: {}", major.getName());
                return Result.error("专业已存在");
            }
            majorService.addMajor(major);
            logger.info("专业创建成功，专业ID: {}, 专业名称: {}", major.getId(), major.getName());
            return Result.success(major); // 返回创建的专业对象，包含ID
        } catch (Exception e) {
            logger.error("创建专业失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建学科
     */
    @PostMapping("/discipline")
    public Result createDiscipline(@RequestBody Subject subject) {
        try {
            logger.info("创建学科请求，学科名称: {}, 专业ID: {}", subject.getName(), subject.getMajorId());
            // 检查学科是否已存在
            Subject existingSubject = subjectService.findByName(subject.getName());
            if (existingSubject != null) {
                logger.warn("学科已存在，学科名称: {}", subject.getName());
                return Result.error("学科已存在");
            }
            subjectService.addSubject(subject);
            logger.info("学科创建成功，学科ID: {}, 学科名称: {}, 专业ID: {}", subject.getId(), subject.getName(), subject.getMajorId());
            return Result.success(subject); // 返回创建的学科对象，包含ID
        } catch (Exception e) {
            logger.error("创建学科失败", e);
            return Result.error(e.getMessage());
        }
    }
}