package com.example.enums;

import java.util.Objects;

/**
 * 系统角色枚举
 *
 */
public enum RoleEnum {

    /**
     * 管理员角色
     */
    ADMIN(1, "管理员"),

    /**
     * 学员角色
     */
    STUDENT(2, "学员");

    /**
     * 角色代码
     */
    private final Integer code;

    /**
     * 角色名称
     */
    private final String name;

    /**
     * 构造函数
     *
     * @param code 角色代码
     * @param name 角色名称
     */
    RoleEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据角色代码获取对应的枚举实例
     *
     * @param code 角色代码
     * @return 对应的枚举实例，如果未找到则返回 null
     */
    public static RoleEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RoleEnum role : values()) {
            if (Objects.equals(role.getCode(), code)) {
                return role;
            }
        }
        return null;
    }

    /**
     * 根据角色名称获取对应的枚举实例
     *
     * @param name 角色名称
     * @return 对应的枚举实例，如果未找到则返回 null
     */
    public static RoleEnum getByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (RoleEnum role : values()) {
            if (role.getName().equals(name)) {
                return role;
            }
        }
        return null;
    }

    // --- Getters ---

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}