package com.example.entity;

import java.util.Date;

/**
 * 帖子实体类
 */
public class Post {
    private Integer id;             // 帖子ID
    private String title;          // 帖子标题
    private String content;        // 帖子内容
    private Integer userId;        // 发帖人ID
    private String username;       // 发帖人姓名
    private Date createTime;       // 发帖时间
    private Date updateTime;       // 更新时间
    private Integer status;        // 状态：0-已保存，1-已发布，2-下架
    private Integer isTop;         // 是否置顶：0-否，1-是
    private String offlineReason;  // 下架原因

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsTop() {
        return isTop;
    }

    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }

    public String getOfflineReason() {
        return offlineReason;
    }

    public void setOfflineReason(String offlineReason) {
        this.offlineReason = offlineReason;
    }
}