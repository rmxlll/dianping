package com.dianping.common.utils;

/**
 * 统一的返回代码
 */
public enum ResultCode {
    // 处理成功
    SUCCESS("10000"),
    // 未知异常
    UNEXPECTED_FAILED("20000"),
    // 鉴权异常
    INVALID_AUTH("20001"),
    // 业务处理错误
    BIZ_FAILED("40004");

    // 存储枚举对应的代码值
    private final String value;

    /**
     * 构造器
     * @param value
     */
    ResultCode(String value) {
        this.value=value;
    }

    /**
     * 重载toString，返回代码值
     * @return
     */
    @Override
    public String toString() {
        return this.value;
    }
}
