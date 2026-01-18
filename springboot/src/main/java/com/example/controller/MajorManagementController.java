package com.example.controller;

import com.example.entity.Major;
import com.example.entity.exam.Subject;
import com.example.service.MajorService;
import com.example.service.exam.SubjectService;
import com.example.common.Result;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/major-management")
public class MajorManagementController {

    private static final Logger logger = LoggerFactory.getLogger(MajorManagementController.class);

    @Resource
    private MajorService majorService;
    
    @Resource
    private SubjectService subjectService;

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
     * 根据ID获取专业信息
     */
    @GetMapping("/major/{id}")
    public Result getMajorById(@PathVariable Integer id) {
        try {
            Major major = majorService.findById(id);
            if (major != null) {
                return Result.success(major);
            } else {
                return Result.error("专业不存在");
            }
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
     * 更新专业
     */
    @PutMapping("/major")
    public Result updateMajor(@RequestBody Major major) {
        try {
            majorService.updateMajor(major);
            return Result.success("专业更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除专业
     */
    @DeleteMapping("/major/{id}")
    public Result deleteMajor(@PathVariable Integer id) {
        try {
            majorService.deleteById(id);
            return Result.success("专业删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取所有学科列表
     */
    @GetMapping("/subjects")
    public Result getAllSubjects() {
        try {
            List<Subject> subjects = subjectService.findAll();
            return Result.success(subjects);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据ID获取学科信息
     */
    @GetMapping("/subject/{id}")
    public Result getSubjectById(@PathVariable Integer id) {
        try {
            Subject subject = subjectService.findById(id);
            if (subject != null) {
                return Result.success(subject);
            } else {
                return Result.error("学科不存在");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建学科
     */
    @PostMapping("/subject")
    public Result createSubject(@RequestBody Subject subject) {
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
    
    /**
     * 更新学科
     */
    @PutMapping("/subject")
    public Result updateSubject(@RequestBody Subject subject) {
        try {
            subjectService.updateSubject(subject);
            return Result.success("学科更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除学科
     */
    @DeleteMapping("/subject/{id}")
    public Result deleteSubject(@PathVariable Integer id) {
        try {
            subjectService.deleteById(id);
            return Result.success("学科删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据专业ID获取学科列表
     */
    @GetMapping("/subjects/by-major/{majorId}")
    public Result getSubjectsByMajor(@PathVariable Integer majorId) {
        try {
            List<Subject> subjects = subjectService.findByMajorId(majorId);
            return Result.success(subjects);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}