package com.dianping.common.utils;

public enum UniformExceptionSubCodeDefine {
    NA("NA"),

    // 20001
    BLANK_USER_TOKEN("BLANK_USER_TOKEN"),
    INVALID_USER_TOKEN("INVALID_USER_TOKEN")
    ;

    private final String subCode;
    UniformExceptionSubCodeDefine(String subCode) {
        this.subCode = subCode;
    }

    @Override
    public String toString() {
        return this.subCode;
    }
}
