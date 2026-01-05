package com.example.entity.exam;

import java.util.Date;
import java.util.List;

/**
 * 题目实体类
 */
public class Question {
    private Integer id; // 题目ID
    private Integer subjectId; // 学科ID
    private String type; // 题目类型：SINGLE_CHOICE(单选题)、MULTIPLE_CHOICE(多选题)、TRUE_FALSE(判断题)、FILL_BLANK(填空题)、ESSAY(问答题)
    private String content; // 题目内容
    private String answer; // 答案
    private String analysis; // 解析
    private String chapter; // 章节
    private String difficulty; // 难度：EASY(简单)、MEDIUM(中等)、HARD(困难)
    private Date createdTime; // 创建时间
    private Date updatedTime; // 更新时间
    
    // 关联的选项列表（对于选择题）
    private List<QuestionOption> options;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public List<QuestionOption> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOption> options) {
        this.options = options;
    }
}