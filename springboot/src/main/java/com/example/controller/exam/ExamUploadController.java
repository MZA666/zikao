package com.example.controller.exam;

import com.example.common.Result;
import com.example.entity.exam.Question;
import com.example.entity.exam.QuestionOption;
import com.example.service.exam.QuestionOptionService;
import com.example.service.exam.QuestionService;
import com.example.utils.WordExamParserUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/exam/upload")
public class ExamUploadController {

    @Resource
    private QuestionService questionService;
    
    @Resource
    private QuestionOptionService optionService;

    /**
     * 上传Word题库文件
     */
    @PostMapping("/bank")
    public Result uploadExamBank(@RequestParam("file") MultipartFile file,
                                 @RequestParam("subjectId") Integer subjectId,
                                 @RequestParam(value = "subjectName", required = false) String subjectName,
                                 @RequestParam(value = "userId", required = false) Integer userId,
                                 @RequestParam(value = "username", required = false) String username) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 检查文件类型
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.toLowerCase().endsWith(".doc") && !fileName.toLowerCase().endsWith(".docx"))) {
            return Result.error("只支持.doc和.docx格式的文件");
        }

        // 验证必要参数
        if (subjectId == null) {
            return Result.error("学科ID不能为空");
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
                // 设置上传者信息（如果提供了的话）
                if (userId != null) {
                    question.setUploaderId(userId);
                }
                if (username != null) {
                    question.setUploader(username);
                }
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

    /**
     * 批量上传题目
     */
    @PostMapping("/batch")
    public Result batchUploadQuestions(@RequestBody Map<String, Object> requestData) {
        try {
            // 获取题目列表
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> questions = 
                (java.util.List<Map<String, Object>>) requestData.get("questions");
            
            // 获取用户信息
            Integer userId = (Integer) requestData.get("userId");
            String username = (String) requestData.get("username");
            
            if (questions == null || questions.isEmpty()) {
                return Result.error("题目列表不能为空");
            }

            int successCount = 0;
            for (Map<String, Object> questionData : questions) {
                // 创建Question对象
                Question question = new Question();
                
                // 设置基本属性
                Integer subjectId = (Integer) questionData.get("subjectId");
                if (subjectId == null) {
                    continue; // 跳过无效题目
                }
                
                question.setSubjectId(subjectId);
                question.setType((String) questionData.get("type"));
                question.setContent((String) questionData.get("content"));
                question.setAnswer((String) questionData.get("answer"));
                
                // 设置上传者信息
                question.setUploaderId(userId);
                question.setUploader(username);
                
                // 安全地获取可能为null的字段
                Object analysisObj = questionData.get("analysis");
                question.setAnalysis(analysisObj != null ? analysisObj.toString() : null);
                
                Object chapterObj = questionData.get("chapter");
                question.setChapter(chapterObj != null ? chapterObj.toString() : null);
                
                Object difficultyObj = questionData.get("difficulty");
                question.setDifficulty(difficultyObj != null ? difficultyObj.toString() : "MEDIUM");
                
                // 处理选项
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> options = 
                    (java.util.List<Map<String, Object>>) questionData.get("options");
                
                if (options != null && !options.isEmpty()) {
                    // 如果是选择题，创建选项列表
                    java.util.List<QuestionOption> questionOptions = new java.util.ArrayList<>();
                    
                    for (int i = 0; i < options.size(); i++) {
                        Map<String, Object> optionData = options.get(i);
                        
                        QuestionOption option = new QuestionOption();
                        option.setOptionKey((String) optionData.get("label"));
                        Object contentObj = optionData.get("content");
                        option.setContent(contentObj != null ? contentObj.toString() : "");
                        Object isCorrectObj = optionData.get("isCorrect");
                        option.setIsCorrect(isCorrectObj != null ? (Boolean) isCorrectObj : false);
                        option.setSortOrder(i + 1);
                        
                        questionOptions.add(option);
                    }
                    
                    // 使用服务层的批量插入方法
                    int result = questionService.insert(question);
                    if (result > 0) {
                        // 获取刚插入的题目ID，然后插入选项
                        Integer questionId = question.getId();
                        for (QuestionOption option : questionOptions) {
                            option.setQuestionId(questionId);
                            optionService.insert(option);
                        }
                        successCount++;
                    }
                } else {
                    // 没有选项的题目，直接插入
                    int result = questionService.insert(question);
                    if (result > 0) {
                        successCount++;
                    }
                }
            }

            return Result.success("成功批量上传" + successCount + "道题目");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量上传失败: " + e.getMessage());
        }
    }
}