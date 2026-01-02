package com.example.enums;


import java.util.Objects;

/**
 * 用户状态枚举
 *
 * @author YourName
 * @date 2023-10-27
 */
public enum UserStatusEnum {

    /**
     * 启用状态
     */
    ENABLED(1, "启用"),

    /**
     * 禁用/删除状态
     */
    DISABLED(0, "禁用");

    private final Integer code;
    private final String name;

    UserStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static UserStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserStatusEnum status : values()) {
            if (Objects.equals(status.getCode(), code)) {
                return status;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}