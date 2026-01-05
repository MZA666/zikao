package com.example.entity.exam;

import java.util.Date;

/**
 * 题目收藏实体类
 */
public class QuestionCollection {
    private Integer id; // 收藏ID
    private Integer userId; // 用户ID
    private Integer questionId; // 题目ID
    private Date collectedTime; // 收藏时间

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

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Date getCollectedTime() {
        return collectedTime;
    }

    public void setCollectedTime(Date collectedTime) {
        this.collectedTime = collectedTime;
    }
}