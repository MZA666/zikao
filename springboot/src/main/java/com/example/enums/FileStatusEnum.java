package com.example.enums;

/**
 * 文件审核状态枚举
 */
public enum FileStatusEnum {
    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝");

    private final Integer code;
    private final String name;

    FileStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static FileStatusEnum getByCode(Integer code) {
        for (FileStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}