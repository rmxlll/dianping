package com.dianping.common.utils;

import cn.hutool.core.util.StrUtil;

/**
 * 统一的异常抽象类
 */
public abstract class GenericException extends Exception {
    // 异常代码
    private final ResultCode exceptionResultCode;
    // 异常子代码
    private UniformExceptionSubCodeDefine subCode;
    // 异常描述
    private String message;

    /**
     * 构造器
     * @param resultCode
     * @param subCode
     * @param message
     * @param e
     */
    public GenericException(ResultCode resultCode, UniformExceptionSubCodeDefine subCode, String message, Throwable e) {
        super(e);
        if(resultCode != null) {
            this.exceptionResultCode = resultCode;
        } else {
            this.exceptionResultCode = ResultCode.UNEXPECTED_FAILED;
        }

        if(subCode != null && !UniformExceptionSubCodeDefine.NA.equals(subCode)) {
            this.subCode = subCode;
        } else {
            this.subCode = UniformExceptionSubCodeDefine.NA;
        }
        this.message = message;
    }

    /**
     * 组装异常描述信息
     * @return
     */
    @Override
    public String getMessage() {
        StringBuilder msg = new StringBuilder("[").append(exceptionResultCode);
        if(subCode != null && !UniformExceptionSubCodeDefine.NA.equals(subCode)) {
            msg.append("-").append(subCode);
        } else {
            this.subCode = UniformExceptionSubCodeDefine.NA;
        }
        msg.append("]");

        if(!StrUtil.isBlank(message)) {
            msg.append(message);
        }
        if(this.getCause() != null) {
            msg.append(" {").append(this.getCause().getMessage()).append("}");
        }

        return msg.toString();
    }

    /**
     * 添加异常描述信息
     * @param additionalMessage
     */
    public void appendMessage(String additionalMessage) {
        if(StrUtil.isBlank(additionalMessage)) {
            return;
        }

        if(StrUtil.isBlank(this.message)) {
            this.message = additionalMessage;
        } else {
            this.message = this.message + "\n" + additionalMessage;
        }
    }

    /**
     * 获取错误代码
     * @return
     */
    public ResultCode getResultCode() {
        return this.exceptionResultCode;
    }

    /**
     * 获取错误子代码
     * @return
     */
    public UniformExceptionSubCodeDefine getSubCode() {
        return this.subCode;
    }
}
