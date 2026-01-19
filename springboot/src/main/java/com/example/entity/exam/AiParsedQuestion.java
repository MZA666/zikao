package com.example.entity.exam;

import java.util.List;

/**
 * AI解析题目结果的数据传输对象
 */
public class AiParsedQuestion {
    private String type; // 题目类型：SINGLE_CHOICE(单选题)、MULTIPLE_CHOICE(多选题)、TRUE_FALSE(判断题)、FILL_BLANK(填空题)、ESSAY(问答题)
    private String content; // 题目内容
    private String answer; // 答案
    private String analysis; // 解析
    private String difficulty; // 难度：EASY(简单)、MEDIUM(中等)、HARD(困难)
    private List<AiParsedOption> options; // 选项列表（仅适用于选择题）

    // Getters and Setters
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

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public List<AiParsedOption> getOptions() {
        return options;
    }

    public void setOptions(List<AiParsedOption> options) {
        this.options = options;
    }
}