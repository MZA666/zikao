package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.enums.RoleEnum;
import com.example.entity.SysUser;
import com.example.enums.UserStatusEnum;
import com.example.exception.CustomException;
import com.example.mapper.AdminMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 管理员业务处理
 **/
@Service
public class AdminService {

    @Resource
    private AdminMapper adminMapper;

    /**
     * 新增
     */
    public void add(SysUser admin) {
        SysUser dbAdmin = adminMapper.selectByUsername(admin.getUsername());
        if (ObjectUtil.isNotNull(dbAdmin)) {
            throw new CustomException("用户不存在");
        }
        if (ObjectUtil.isEmpty(admin.getPassword())) {
            admin.setPassword("admin");
        }
        if (ObjectUtil.isEmpty(admin.getName())) {
            admin.setName(admin.getUsername());
        }
        admin.setRoleId("管理员");
        adminMapper.insert(admin);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        adminMapper.deleteById(id);
    }

    /**
     * 修改
     */
    public void updateById(SysUser admin) {
        adminMapper.updateById(admin);
    }

    /**
     * 根据ID查询
     */
    public SysUser selectById(Integer id) {
        return adminMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<SysUser> selectAll(SysUser admin) {
        return adminMapper.selectAll(admin);
    }

    /**
     * 分页查询
     */
    public PageInfo<SysUser> selectPage(SysUser admin, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<SysUser> list = adminMapper.selectAll(admin);
        return PageInfo.of(list);
    }

    /**
     * 登录
     */
    public SysUser login(SysUser sysUser) {
        SysUser dbAdmin = adminMapper.selectByUsername(sysUser.getUsername());
        if (ObjectUtil.isNull(dbAdmin)) {
            throw new CustomException("用户不存在");
        }
        if (!sysUser.getPassword().equals(dbAdmin.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        return dbAdmin;
    }

    /**
     * 修改密码
     */
    public void updatePassword(SysUser sysUser) {
        SysUser dbAdmin = adminMapper.selectByUsername(sysUser.getUsername());
        if (ObjectUtil.isNull(dbAdmin)) {
            throw new CustomException("用户不存在");
        }
        if (!sysUser.getPassword().equals(dbAdmin.getPassword())) {
            throw new CustomException("原密码错误");
        }
       // dbAdmin.setPassword(account.getNewPassword());
        adminMapper.updateById(dbAdmin);
    }

    /**
     * 注册
     * @param sysUser
     */
    public void register(SysUser sysUser) {
        //判断用户是否注册过
        SysUser user = adminMapper.selectByUsername(sysUser.getUsername());
        if (ObjectUtil.isNotNull(user)) {
            throw new CustomException("用户已存在");
        }
        //只开放学员注册，所有角色为2
        sysUser.setRoleId(RoleEnum.STUDENT.getCode().toString());
        sysUser.setStatus(UserStatusEnum.ENABLED.getCode().toString());
        sysUser.setCreateTime(new Date());
        adminMapper.insert(sysUser);
    }
}