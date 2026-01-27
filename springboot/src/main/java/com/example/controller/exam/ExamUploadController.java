package com.example.controller.exam;

import com.example.common.Result;
import com.example.entity.Major;
import com.example.entity.exam.ExamBank;
import com.example.entity.exam.Question;
import com.example.entity.exam.QuestionOption;
import com.example.service.MajorService;
import com.example.service.exam.AiQuestionParseService;
import com.example.service.exam.ExamBankService;
import com.example.service.exam.QuestionOptionService;
import com.example.service.exam.QuestionService;
import com.example.utils.FileUtil;
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
    
    @Resource
    private AiQuestionParseService aiQuestionParseService;
    
    @Resource
    private MajorService majorService;
    
    @Resource
    private ExamBankService examBankService;

    /**
     * 上传Word题库文件
     */
    @PostMapping("/bank")
    public Result uploadExamBank(@RequestParam("file") MultipartFile file,
                                 @RequestParam("subjectId") Integer subjectId,
                                 @RequestParam(value = "bankName", required = false) String bankName,
                                 @RequestParam(value = "majorId", required = false) Integer majorId,
                                 @RequestParam(value = "majorName", required = false) String majorName,
                                 @RequestParam(value = "subjectName", required = false) String subjectName,
                                 @RequestParam(value = "userId", required = false) Integer userId,
                                 @RequestParam(value = "username", required = false) String username) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 检查文件类型
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.toLowerCase().endsWith(".doc") && 
                                 !fileName.toLowerCase().endsWith(".docx") && 
                                 !fileName.toLowerCase().endsWith(".txt"))) {
            return Result.error("只支持.doc、.docx和.txt格式的文件");
        }

        // 验证必要参数
        if (subjectId == null) {
            return Result.error("学科ID不能为空");
        }
        
        if (bankName == null || bankName.trim().isEmpty()) {
            return Result.error("题库名称不能为空");
        }

        // 如果专业名称存在但专业ID为空，尝试查找或创建专业
        Integer resolvedMajorId = majorId;
        if (majorName != null && !majorName.trim().isEmpty() && resolvedMajorId == null) {
            Major existingMajor = majorService.findByName(majorName);
            if (existingMajor != null) {
                resolvedMajorId = existingMajor.getId();
            } else {
                // 创建新的专业
                Major newMajor = new Major();
                newMajor.setName(majorName);
                newMajor.setDescription("由题库上传自动创建");
                majorService.addMajor(newMajor);
                resolvedMajorId = newMajor.getId();
            }
        }

        // 检查是否已存在同名题库（同一学科、同一上传人），如果存在则使用现有题库
        ExamBank existingBank = null;
        List<ExamBank> banks = examBankService.selectAll(new ExamBank() {{
            setBankName(bankName);
            setSubject(subjectName);
            setUploaderId(userId);
        }});
        if (banks != null && !banks.isEmpty()) {
            existingBank = banks.get(0); // 使用第一个匹配的题库
        }
        
        Integer bankId;
        if (existingBank != null) {
            bankId = existingBank.getBankId();
            // 更新已存在题库的专业和学科信息（如果为空或需要更新）
            boolean needUpdate = false;
            if (existingBank.getMajorId() == null && resolvedMajorId != null) {
                existingBank.setMajorId(resolvedMajorId);
                needUpdate = true;
            }
            if ((existingBank.getMajorName() == null || existingBank.getMajorName().isEmpty()) && majorName != null) {
                existingBank.setMajorName(majorName);
                needUpdate = true;
            }
            if (existingBank.getSubjectId() == null && subjectId != null) {
                existingBank.setSubjectId(subjectId);
                needUpdate = true;
            }
            if (needUpdate) {
                examBankService.updateExamBank(existingBank);
            }
        } else {
            // 创建题库记录
            ExamBank examBank = new ExamBank();
            examBank.setBankName(bankName);
            examBank.setMajorId(resolvedMajorId); // 设置专业ID
            examBank.setMajorName(majorName); // 设置专业名称
            examBank.setSubject(subjectName); // 使用学科名称
            examBank.setSubjectId(subjectId); // 设置学科ID
            examBank.setUploaderId(userId);
            examBank.setUploader(username);
            examBank.setQuestionCount(0); // 初始化为0，后续更新
            examBank.setStatus(1); // 设置为已通过状态
            examBank.setIsShared(0); // 默认私密
            examBankService.createExamBank(examBank);
            bankId = examBank.getBankId(); // 获取新创建的题库ID
        }
        
        // 创建临时文件
        File tempFile = null;
        try {
            // 生成唯一文件名
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            tempFile = new File(System.getProperty("java.io.tmpdir"), uniqueFileName);
            
            // 保存上传的文件到临时位置
            file.transferTo(tempFile);

            // 使用AI解析服务解析题目
            List<Question> questions = aiQuestionParseService.parseQuestionsFromFile(tempFile.getAbsolutePath());

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
                // 设置题库ID
                question.setBankId(bankId);
                // 设置专业ID
                if (resolvedMajorId != null) {
                    question.setMajorId(resolvedMajorId);
                }
                // 设置默认难度
                if (question.getDifficulty() == null) {
                    question.setDifficulty("MEDIUM");
                }
                
                // 先保存题目
                int result = questionService.insert(question);
                if (result > 0) {
                    // 如果有选项，保存选项
                    if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                        for (QuestionOption option : question.getOptions()) {
                            option.setQuestionId(question.getId()); // 设置题目ID
                            optionService.insert(option);
                        }
                    }
                    successCount++;
                }
            }
            
            // 更新题库中的题目数量
            if (existingBank == null) { // 仅在新建题库时更新计数
                ExamBank examBank = examBankService.selectById(bankId);
                if (examBank != null) {
                    examBank.setQuestionCount(examBank.getQuestionCount() + successCount);
                    examBankService.updateExamBank(examBank);
                }
            } else { // 如果是已有题库，也要更新计数
                ExamBank examBank = examBankService.selectById(bankId);
                if (examBank != null) {
                    examBank.setQuestionCount(examBank.getQuestionCount() + successCount);
                    examBankService.updateExamBank(examBank);
                }
            }

            return Result.success("成功解析并保存" + successCount + "道题目到题库：" + bankName);
        } catch (Exception e) {
            e.printStackTrace();
            // 如果出错，删除刚创建的题库记录（仅在新建题库时）
            if (existingBank == null && bankId != null) {
                examBankService.deleteExamBankById(bankId);
            }
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
            
            // 获取题库和专业信息
            String bankName = (String) requestData.get("bankName");
            Integer majorId = (Integer) requestData.get("majorId");
            String majorName = (String) requestData.get("majorName");
            String subjectName = (String) requestData.get("subjectName");
            Integer subjectId = (Integer) requestData.get("subjectId");
            
            System.out.println("Debug Batch Upload: 接收到的参数 - bankName: " + bankName + ", majorName: " + majorName + ", majorId: " + majorId + ", subjectName: " + subjectName + ", subjectId: " + subjectId);
            
            if (questions == null || questions.isEmpty()) {
                return Result.error("题目列表不能为空");
            }
            
            if (bankName == null || bankName.trim().isEmpty()) {
                return Result.error("题库名称不能为空");
            }
            
            if (subjectId == null) {
                return Result.error("学科ID不能为空");
            }

            // 如果专业名称存在但专业ID为空，尝试查找或创建专业
            Integer resolvedMajorId = majorId;
            if (majorName != null && !majorName.trim().isEmpty() && resolvedMajorId == null) {
                Major existingMajor = majorService.findByName(majorName);
                if (existingMajor != null) {
                    resolvedMajorId = existingMajor.getId();
                    System.out.println("Debug Batch: 找到现有专业，ID: " + resolvedMajorId);
                } else {
                    // 创建新的专业
                    Major newMajor = new Major();
                    newMajor.setName(majorName);
                    newMajor.setDescription("由题库上传自动创建");
                    majorService.addMajor(newMajor);
                    resolvedMajorId = newMajor.getId();
                    System.out.println("Debug Batch: 创建新专业，ID: " + resolvedMajorId);
                }
            } else {
                System.out.println("Debug Batch: 专业ID已存在或专业名称为空，resolvedMajorId: " + resolvedMajorId + ", majorName: " + majorName);
            }

            // 检查是否已存在同名题库（同一学科、同一上传人），如果存在则使用现有题库
            ExamBank existingBank = null;
            List<ExamBank> banks = examBankService.selectAll(new ExamBank() {{
                setBankName(bankName);
                setSubject(subjectName);
                setUploaderId(userId);
            }});
            if (banks != null && !banks.isEmpty()) {
                existingBank = banks.get(0); // 使用第一个匹配的题库
            }
            
            Integer bankId;
            if (existingBank != null) {
                bankId = existingBank.getBankId();
                System.out.println("Debug Batch: 找到已存在的题库，bankId: " + bankId + ", 当前majorName: " + existingBank.getMajorName() + ", 待设置majorName: " + majorName);
                // 更新已存在题库的专业和学科信息（如果为空或需要更新）
                boolean needUpdate = false;
                if (existingBank.getMajorId() == null && resolvedMajorId != null) {
                    existingBank.setMajorId(resolvedMajorId);
                    needUpdate = true;
                }
                if ((existingBank.getMajorName() == null || existingBank.getMajorName().isEmpty()) && majorName != null) {
                    existingBank.setMajorName(majorName);
                    needUpdate = true;
                }
                if (existingBank.getSubjectId() == null && subjectId != null) {
                    existingBank.setSubjectId(subjectId);
                    needUpdate = true;
                }
                if (needUpdate) {
                    examBankService.updateExamBank(existingBank);
                    System.out.println("Debug Batch: 已更新题库信息");
                }
            } else {
                // 创建题库记录
                ExamBank examBank = new ExamBank();
                examBank.setBankName(bankName);
                examBank.setMajorId(resolvedMajorId); // 设置专业ID
                examBank.setMajorName(majorName); // 设置专业名称
                examBank.setSubject(subjectName); // 使用学科名称
                examBank.setSubjectId(subjectId); // 设置学科ID
                examBank.setUploaderId(userId);
                examBank.setUploader(username);
                examBank.setQuestionCount(0); // 初始化为0，后续更新
                examBank.setStatus(1); // 设置为已通过状态
                examBank.setIsShared(0); // 默认私密
                System.out.println("Debug Batch: 创建新题库 - bankName: " + bankName + ", majorId: " + resolvedMajorId + ", majorName: " + majorName + ", subjectId: " + subjectId + ", subjectName: " + subjectName);
                examBankService.createExamBank(examBank);
                bankId = examBank.getBankId(); // 获取新创建的题库ID
                System.out.println("Debug Batch: 新题库创建成功，bankId: " + bankId);
            }

            int successCount = 0;
            for (Map<String, Object> questionData : questions) {
                // 创建Question对象
                Question question = new Question();
                
                // 设置基本属性
                question.setSubjectId(subjectId);
                question.setType((String) questionData.get("type"));
                question.setContent((String) questionData.get("content"));
                question.setAnswer((String) questionData.get("answer"));
                
                // 设置上传者信息
                question.setUploaderId(userId);
                question.setUploader(username);
                
                // 设置题库ID
                question.setBankId(bankId);
                
                // 设置专业ID
                if (resolvedMajorId != null) {
                    question.setMajorId(resolvedMajorId);
                }
                
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
            
            // 更新题库中的题目数量
            if (existingBank == null) { // 仅在新建题库时更新计数
                ExamBank examBank = examBankService.selectById(bankId);
                if (examBank != null) {
                    examBank.setQuestionCount(examBank.getQuestionCount() + successCount);
                    examBankService.updateExamBank(examBank);
                }
            } else { // 如果是已有题库，也要更新计数
                ExamBank examBank = examBankService.selectById(bankId);
                if (examBank != null) {
                    examBank.setQuestionCount(examBank.getQuestionCount() + successCount);
                    examBankService.updateExamBank(examBank);
                }
            }

            return Result.success("成功批量上传" + successCount + "道题目到题库：" + bankName);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量上传失败: " + e.getMessage());
        }
    }
}