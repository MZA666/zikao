package com.example.controller.exam;

import com.example.common.Result;
import com.example.entity.exam.Question;
import com.example.service.exam.QuestionService;
import com.example.utils.WordExamParserUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 题库上传控制器
 */
@RestController
@RequestMapping("/exam/upload")
public class ExamUploadController {

    @Resource
    private QuestionService questionService;

    /**
     * 上传Word题库文件
     */
    @PostMapping("/bank")
    public Result uploadExamBank(@RequestParam("file") MultipartFile file, 
                                 @RequestParam("subjectId") Integer subjectId,
                                 @RequestParam(value = "subjectName", required = false) String subjectName) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 检查文件类型
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.toLowerCase().endsWith(".doc") && !fileName.toLowerCase().endsWith(".docx"))) {
            return Result.error("只支持.doc和.docx格式的文件");
        }

        // 创建临时文件
        File tempFile = null;
        try {
            // 生成唯一文件名
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            tempFile = new File(System.getProperty("java.io.tmpdir"), uniqueFileName);
            
            // 保存上传的文件到临时位置
            file.transferTo(tempFile);

            // 解析Word文件中的题目
            List<Question> questions = WordExamParserUtil.parseExamQuestions(tempFile.getAbsolutePath());

            if (questions.isEmpty()) {
                return Result.error("未从文件中解析到任何题目");
            }

            // 保存题目到数据库
            int successCount = 0;
            for (Question question : questions) {
                question.setSubjectId(subjectId);
                // 设置默认难度
                if (question.getDifficulty() == null) {
                    question.setDifficulty("MEDIUM");
                }
                int result = questionService.insert(question);
                if (result > 0) {
                    successCount++;
                }
            }

            return Result.success("成功解析并保存" + successCount + "道题目");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件解析失败: " + e.getMessage());
        } finally {
            // 删除临时文件
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}