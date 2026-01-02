package com.example.mapper;

import com.example.entity.SysUser;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作admin相关数据接口
*/
public interface AdminMapper {

    /**
      * 新增
    */
    int insert(SysUser admin);

    /**
      * 删除
    */
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(SysUser admin);

    /**
      * 根据ID查询
    */
    SysUser selectById(Integer id);

    /**
      * 查询所有
    */
    List<SysUser> selectAll(SysUser admin);

    @Select("select * from `sys_user` where username = #{username}")
    SysUser selectByUsername(String username);

}