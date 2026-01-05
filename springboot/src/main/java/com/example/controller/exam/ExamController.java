package com.example.controller.exam;

import com.example.common.Result;
import com.example.entity.exam.*;
import com.example.service.exam.*;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
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

    // 生成模拟考试试卷
    @PostMapping("/paper/generate")
    public Result generateExamPaper(@RequestParam Integer subjectId,
                                   @RequestParam Integer questionCount,
                                   @RequestParam(required = false) String questionTypes) {
        try {
            // 随机获取指定数量和类型的题目
            List<Question> questions;
            if (questionTypes != null && !questionTypes.isEmpty()) {
                // 如果指定了题目类型，按类型获取
                String[] types = questionTypes.split(",");
                questions = questionService.selectRandomQuestions(subjectId, types[0], questionCount);
            } else {
                // 否则获取任意类型
                questions = questionService.selectRandomQuestions(subjectId, null, questionCount);
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
}