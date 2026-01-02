package com.example.controller;

import com.example.common.Result;
import com.example.entity.SysUser;
import com.example.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


@RestController
public class WebController {

    @Resource
    private AdminService adminService;


    /**
     * 默认请求接口
     */
    @GetMapping("/")
    public Result hello() {
        return Result.success();
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody SysUser sysUser) {
        SysUser user = null;

        user = adminService.login(sysUser);

        return Result.success(user);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody SysUser sysUser) {
        adminService.register(sysUser);
        return Result.success(sysUser);
    }

    /**
     * 修改密码
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody SysUser sysUser) {
        if ("ADMIN".equals(sysUser.getRoleId())) {
            adminService.updatePassword(sysUser);
        }
        return Result.success();
    }

}
