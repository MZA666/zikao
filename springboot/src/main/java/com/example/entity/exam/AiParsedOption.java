package com.example.entity.exam;

/**
 * AI解析题目选项的数据传输对象
 */
public class AiParsedOption {
    private String optionKey; // 选项键，如"A"、"B"、"C"、"D"
    private String content; // 选项内容
    private Boolean isCorrect; // 是否为正确答案

    // Getters and Setters
    public String getOptionKey() {
        return optionKey;
    }

    public void setOptionKey(String optionKey) {
        this.optionKey = optionKey;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
}