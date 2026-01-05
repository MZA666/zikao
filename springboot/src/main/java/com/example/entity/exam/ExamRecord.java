package com.example.entity.exam;

import java.util.Date;
import java.util.List;

/**
 * 考试记录实体类
 */
public class ExamRecord {
    private Integer id; // 记录ID
    private Integer userId; // 用户ID
    private String username; // 用户名
    private Integer paperId; // 试卷ID
    private String paperName; // 试卷名称
    private Integer subjectId; // 学科ID
    private String subjectName; // 学科名称
    private Integer score; // 得分
    private Integer totalScore; // 总分
    private Integer accuracy; // 正确率
    private Integer duration; // 考试用时（分钟）
    private Date examTime; // 考试时间
    private List<ExamAnswer> answers; // 答题详情

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getPaperId() {
        return paperId;
    }

    public void setPaperId(Integer paperId) {
        this.paperId = paperId;
    }

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Integer accuracy) {
        this.accuracy = accuracy;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Date getExamTime() {
        return examTime;
    }

    public void setExamTime(Date examTime) {
        this.examTime = examTime;
    }

    public List<ExamAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ExamAnswer> answers) {
        this.answers = answers;
    }
}