package com.example.service.impl;

import com.example.entity.Post;
import com.example.mapper.PostMapper;
import com.example.service.PostService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    @Resource
    private PostMapper postMapper;

    @Override
    public void add(Post post) {
        postMapper.insert(post);
    }

    @Override
    public void deleteById(Integer id) {
        postMapper.deleteById(id);
    }

    @Override
    public void update(Post post) {
        post.setUpdateTime(new java.util.Date());
        postMapper.update(post);
    }

    @Override
    public Post findById(Integer id) {
        return postMapper.selectById(id);
    }

    @Override
    public List<Post> findAll() {
        return postMapper.selectAllPosts();
    }

    @Override
    public PageInfo<Post> findPage(int pageNum, int pageSize, String title, String startTime, String endTime, Integer userId) {
        PageHelper.startPage(pageNum, pageSize);
        List<Post> posts = postMapper.selectList(title, startTime, endTime, userId);
        return new PageInfo<>(posts);
    }

    @Override
    public List<Post> findByUserId(Integer userId) {
        return postMapper.selectByUserId(userId);
    }

    @Override
    public List<Post> findPublishedPosts() {
        return postMapper.selectPublishedPosts();
    }

    @Override
    public void updateTopStatus(Integer id, Integer isTop) {
        postMapper.updateTopStatus(id, isTop);
    }

    @Override
    public void updateOfflineStatus(Integer id, String offlineReason) {
        postMapper.updateOfflineStatus(id, offlineReason);
    }
}