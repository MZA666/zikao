package com.example.controller.exam;

import com.example.common.Result;
import com.example.entity.exam.*;
import com.example.service.exam.*;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 考试相关控制器
 */
@RestController
@RequestMapping("/exam")
public class ExamController {

    @Resource
    private QuestionService questionService;
    
    @Resource
    private QuestionOptionService optionService;
    
    @Resource
    private SubjectService subjectService;
    
    @Resource
    private ExamPaperService examPaperService;
    
    @Resource
    private ExamPaperQuestionService examPaperQuestionService;
    
    @Resource
    private QuestionCollectionService collectionService;
    
    @Resource
    private ExamRecordService examRecordService;
    
    @Resource
    private ExamBankService examBankService;
    
    @Resource
    private ExamBankCollectionService examBankCollectionService;

    // 学科相关接口
    @PostMapping("/subject")
    public Result addSubject(@RequestBody Subject subject) {
        subjectService.insert(subject);
        return Result.success();
    }

    @DeleteMapping("/subject/{id}")
    public Result deleteSubject(@PathVariable Integer id) {
        subjectService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/subject")
    public Result updateSubject(@RequestBody Subject subject) {
        subjectService.updateById(subject);
        return Result.success();
    }

    @GetMapping("/subject/{id}")
    public Result getSubject(@PathVariable Integer id) {
        Subject subject = subjectService.selectById(id);
        return Result.success(subject);
    }

    @GetMapping("/subject/list")
    public Result getSubjectList(Subject subject) {
        List<Subject> subjects = subjectService.selectAll(subject);
        return Result.success(subjects);
    }

    // 题目相关接口
    @PostMapping("/question")
    public Result addQuestion(@RequestBody Question question) {
        questionService.insert(question);
        return Result.success();
    }

    @DeleteMapping("/question/{id}")
    public Result deleteQuestion(@PathVariable Integer id) {
        questionService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/question")
    public Result updateQuestion(@RequestBody Question question) {
        questionService.updateById(question);
        return Result.success();
    }

    @GetMapping("/question/{id}")
    public Result getQuestion(@PathVariable Integer id) {
        Question question = questionService.selectQuestionWithOptions(id);
        return Result.success(question);
    }

    @GetMapping("/question/list")
    public Result getQuestionList(Question question) {
        List<Question> questions = questionService.selectAll(question);
        // 为每个题目获取选项
        for (Question q : questions) {
            List<QuestionOption> options = optionService.selectByQuestionId(q.getId());
            q.setOptions(options);
        }
        return Result.success(questions);
    }

    @GetMapping("/question/subject/{subjectId}")
    public Result getQuestionBySubject(@PathVariable Integer subjectId) {
        List<Question> questions = questionService.selectBySubjectId(subjectId);
        // 为每个题目获取选项
        for (Question q : questions) {
            List<QuestionOption> options = optionService.selectByQuestionId(q.getId());
            q.setOptions(options);
        }
        return Result.success(questions);
    }

    @GetMapping("/question/type/{type}")
    public Result getQuestionByType(@PathVariable String type) {
        List<Question> questions = questionService.selectByType(type);
        // 为每个题目获取选项
        for (Question q : questions) {
            List<QuestionOption> options = optionService.selectByQuestionId(q.getId());
            q.setOptions(options);
        }
        return Result.success(questions);
    }

    // 随机获取题目
    @GetMapping("/question/random")
    public Result getRandomQuestions(@RequestParam Integer subjectId, 
                                    @RequestParam(required = false) String type, 
                                    @RequestParam Integer limit) {
        List<Question> questions = questionService.selectRandomQuestions(subjectId, type, limit);
        // 为每个题目获取选项
        for (Question q : questions) {
            List<QuestionOption> options = optionService.selectByQuestionId(q.getId());
            q.setOptions(options);
        }
        return Result.success(questions);
    }

    // 获取题目统计信息（按学科和上传者分组）
    @GetMapping("/question/stats-by-uploader")
    public Result getQuestionStatsByUploader(@RequestParam(required = false) Integer subjectId) {
        List<Question> questions;
        if (subjectId != null) {
            questions = questionService.selectBySubjectId(subjectId);
        } else {
            questions = questionService.selectAll(new Question());
        }
        
        // 按学科ID和上传者ID分组统计
        Map<String, List<Question>> stats = questions.stream()
            .collect(Collectors.groupingBy(q -> q.getSubjectId() + "_" + (q.getUploaderId() != null ? q.getUploaderId() : "unknown")));
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Question>> entry : stats.entrySet()) {
            String[] keys = entry.getKey().split("_", 2);
            Integer subjId = Integer.parseInt(keys[0]);
            String uploader = entry.getValue().isEmpty() ? "未知" : 
                             (entry.getValue().get(0).getUploader() != null ? 
                              entry.getValue().get(0).getUploader() : 
                              "系统");
            Integer uploaderId = entry.getValue().isEmpty() ? null : entry.getValue().get(0).getUploaderId();
            
            Map<String, Object> item = new HashMap<>();
            item.put("subjectId", subjId);
            item.put("uploader", uploader);
            item.put("uploaderId", uploaderId);
            item.put("questionCount", (long)entry.getValue().size());
            item.put("subjectName", getSubjectName(subjId)); // 获取学科名称
            result.add(item);
        }
        
        return Result.success(result);
    }

    // 根据学科ID和上传者ID获取题目
    @GetMapping("/question/by-uploader")
    public Result getQuestionsByUploader(@RequestParam Integer subjectId, 
                                         @RequestParam(required = false) Integer uploaderId) {
        Question question = new Question();
        question.setSubjectId(subjectId);
        if (uploaderId != null) {
            question.setUploaderId(uploaderId);
        }
        List<Question> questions = questionService.selectAll(question);
        
        // 为每个题目获取选项
        for (Question q : questions) {
            List<QuestionOption> options = optionService.selectByQuestionId(q.getId());
            q.setOptions(options);
        }
        
        return Result.success(questions);
    }
    
    // 获取题目统计信息（按学科分组）
    @GetMapping("/question/stats")
    public Result getQuestionStats(@RequestParam(required = false) Integer subjectId) {
        List<Question> questions;
        if (subjectId != null) {
            questions = questionService.selectBySubjectId(subjectId);
        } else {
            questions = questionService.selectAll(new Question());
        }
        
        // 按学科ID分组统计
        Map<Integer, Long> stats = questions.stream()
            .collect(Collectors.groupingBy(
                Question::getSubjectId,
                Collectors.counting()
            ));
        
        return Result.success(stats);
    }

    // 辅助方法：根据学科ID获取学科名称
    private String getSubjectName(Integer subjectId) {
        if (subjectId == null) return "未知学科";
        Subject subject = subjectService.selectById(subjectId);
        return subject != null ? subject.getName() : "未知学科";
    }

    // 生成模拟考试试卷
    @PostMapping("/paper/generate")
    public Result generateExamPaper(@RequestParam Integer subjectId,
                                   @RequestParam Integer questionCount,
                                   @RequestParam(required = false) String questionTypes,
                                   @RequestParam(required = false) String difficulty) {
        try {
            // 随机获取指定数量、类型和难度的题目
            List<Question> questions;
            if (questionTypes != null && !questionTypes.isEmpty()) {
                // 如果指定了题目类型，按类型获取
                String[] types = questionTypes.split(",");
                if (difficulty != null && !difficulty.isEmpty()) {
                    // 如果同时指定了难度
                    questions = questionService.selectRandomQuestionsByTypeAndDifficulty(subjectId, types[0], difficulty, questionCount);
                } else {
                    // 只指定类型，不指定难度
                    questions = questionService.selectRandomQuestions(subjectId, types[0], questionCount);
                }
            } else {
                // 没有指定题目类型
                if (difficulty != null && !difficulty.isEmpty()) {
                    // 如果指定了难度
                    questions = questionService.selectRandomQuestionsByDifficulty(subjectId, difficulty, questionCount);
                } else {
                    // 没有指定类型和难度
                    questions = questionService.selectRandomQuestions(subjectId, null, questionCount);
                }
            }
            
            // 构建试卷
            ExamPaper paper = new ExamPaper();
            Subject subject = subjectService.selectById(subjectId);
            paper.setName(subject.getName() + "模拟考试");
            paper.setSubjectId(subjectId);
            paper.setTotalQuestions(questions.size());
            paper.setTotalScore(questions.size() * 5); // 每题5分
            paper.setDuration(60); // 默认60分钟
            
            // 这里可以进一步处理试卷生成逻辑
            
            return Result.success(questions);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("生成试卷失败: " + e.getMessage());
        }
    }

    // 试卷相关接口
    @PostMapping("/paper")
    public Result addExamPaper(@RequestBody ExamPaper examPaper) {
        examPaperService.insert(examPaper);
        return Result.success();
    }

    @DeleteMapping("/paper/{id}")
    public Result deleteExamPaper(@PathVariable Integer id) {
        examPaperService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/paper")
    public Result updateExamPaper(@RequestBody ExamPaper examPaper) {
        examPaperService.updateById(examPaper);
        return Result.success();
    }

    @GetMapping("/paper/{id}")
    public Result getExamPaper(@PathVariable Integer id) {
        ExamPaper examPaper = examPaperService.selectById(id);
        return Result.success(examPaper);
    }

    @GetMapping("/paper/list")
    public Result getExamPaperList(ExamPaper examPaper) {
        List<ExamPaper> examPapers = examPaperService.selectAll(examPaper);
        return Result.success(examPapers);
    }

    @GetMapping("/paper/subject/{subjectId}")
    public Result getExamPaperBySubject(@PathVariable Integer subjectId) {
        List<ExamPaper> examPapers = examPaperService.selectBySubjectId(subjectId);
        return Result.success(examPapers);
    }

    // 题目收藏相关接口
    @PostMapping("/collection")
    public Result addCollection(@RequestBody QuestionCollection collection) {
        collectionService.insert(collection);
        return Result.success();
    }

    @DeleteMapping("/collection/{userId}/{questionId}")
    public Result deleteCollection(@PathVariable Integer userId, @PathVariable Integer questionId) {
        collectionService.deleteByUserIdAndQuestionId(userId, questionId);
        return Result.success();
    }

    @GetMapping("/collection/{userId}")
    public Result getCollectionByUser(@PathVariable Integer userId) {
        List<QuestionCollection> collections = collectionService.selectByUserId(userId);
        return Result.success(collections);
    }

    @GetMapping("/collection/check/{userId}/{questionId}")
    public Result checkCollection(@PathVariable Integer userId, @PathVariable Integer questionId) {
        boolean isCollected = collectionService.isCollected(userId, questionId);
        return Result.success(isCollected);
    }
    
    // 题库收藏相关接口
    @PostMapping("/bank/collection")
    public Result addBankCollection(@RequestBody ExamBankCollection collection) {
        // 检查是否已收藏
        if (examBankCollectionService.isCollected(collection.getUserId(), collection.getBankId())) {
            return Result.error("已收藏过此题库");
        }
        examBankCollectionService.insert(collection);
        return Result.success("收藏成功");
    }

    @DeleteMapping("/bank/collection/{userId}/{bankId}")
    public Result deleteBankCollection(@PathVariable Integer userId, @PathVariable Integer bankId) {
        examBankCollectionService.deleteByUserIdAndBankId(userId, bankId);
        return Result.success("取消收藏成功");
    }

    @GetMapping("/bank/user/{userId}/collections")
    public Result getUserCollectedBanks(@PathVariable Integer userId) {
        List<ExamBankCollection> collections = examBankCollectionService.selectByUserId(userId);
        // 返回收藏的题库ID列表
        List<Integer> bankIds = collections.stream().map(ExamBankCollection::getBankId).collect(Collectors.toList());
        return Result.success(bankIds);
    }

    @GetMapping("/bank/collection/check/{userId}/{bankId}")
    public Result checkBankCollection(@PathVariable Integer userId, @PathVariable Integer bankId) {
        boolean isCollected = examBankCollectionService.isCollected(userId, bankId);
        return Result.success(isCollected);
    }
    
    // 考试记录相关接口
    @PostMapping("/record")
    public Result addExamRecord(@RequestBody ExamRecord examRecord) {
        examRecordService.insert(examRecord);
        return Result.success();
    }

    @DeleteMapping("/record/{id}")
    public Result deleteExamRecord(@PathVariable Integer id) {
        examRecordService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/record")
    public Result updateExamRecord(@RequestBody ExamRecord examRecord) {
        examRecordService.updateById(examRecord);
        return Result.success();
    }

    @GetMapping("/record/{id}")
    public Result getExamRecord(@PathVariable Integer id) {
        ExamRecord examRecord = examRecordService.selectById(id);
        return Result.success(examRecord);
    }

    @GetMapping("/record/list")
    public Result getExamRecordList(ExamRecord examRecord) {
        List<ExamRecord> examRecords = examRecordService.selectAll(examRecord);
        return Result.success(examRecords);
    }

    @GetMapping("/record/user/{userId}")
    public Result getExamRecordByUser(@PathVariable Integer userId) {
        List<ExamRecord> examRecords = examRecordService.selectByUserId(userId);
        return Result.success(examRecords);
    }

    @GetMapping("/record/subject/{subjectId}")
    public Result getExamRecordBySubject(@PathVariable Integer subjectId) {
        List<ExamRecord> examRecords = examRecordService.selectBySubjectId(subjectId);
        return Result.success(examRecords);
    }
    
    // 题库相关接口
    @PostMapping("/bank")
    public Result addExamBank(@RequestBody ExamBank examBank) {
        examBankService.insert(examBank);
        return Result.success();
    }

    @DeleteMapping("/bank/{id}")
    public Result deleteExamBank(@PathVariable Integer id) {
        examBankService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/bank")
    public Result updateExamBank(@RequestBody ExamBank examBank) {
        examBankService.updateById(examBank);
        return Result.success();
    }

    @GetMapping("/bank/{id}")
    public Result getExamBank(@PathVariable Integer id) {
        ExamBank examBank = examBankService.selectById(id);
        return Result.success(examBank);
    }

    @GetMapping("/bank/list")
    public Result getExamBankList(ExamBank examBank) {
        List<ExamBank> examBanks = examBankService.selectAll(examBank);
        return Result.success(examBanks);
    }

    @GetMapping("/bank/subject/{subject}")
    public Result getExamBankBySubject(@PathVariable String subject) {
        List<ExamBank> examBanks = examBankService.selectBySubject(subject);
        return Result.success(examBanks);
    }

    @GetMapping("/bank/uploader/{uploaderId}")
    public Result getExamBankByUploader(@PathVariable Integer uploaderId) {
        List<ExamBank> examBanks = examBankService.selectByUploaderId(uploaderId);
        return Result.success(examBanks);
    }
    
    // 题库统计接口
    @GetMapping("/bank/stats")
    public Result getBankStats(@RequestParam(required = false) Integer subjectId) {
        List<ExamBank> banks;
        if (subjectId != null) {
            // 根据学科ID查找对应的学科名称，然后查询题库
            Subject subject = subjectService.selectById(subjectId);
            if (subject != null) {
                banks = examBankService.selectBySubject(subject.getName());
            } else {
                banks = examBankService.selectAll(null);
            }
        } else {
            banks = examBankService.selectAll(null);
        }
        
        return Result.success(banks);
    }
}