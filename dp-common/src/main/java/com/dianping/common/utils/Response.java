package com.dianping.common.utils;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 标准Restful返回对象
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response implements Serializable {
    // 处理结果代码
    // 参考com.hellobike.pmo.cockpit.common.ResultCode
    private String code;
    // 错误子代码
    // 当错误发生时，表示详细错误的类型，一般用户多语言和错误排查
    private String subCode;
    // 错误简略信息
    private String message;
    // 错误详细信息
    private String detailErrorInfo;
    // 请求处理成功后的返回对象
    private Object data;

    /**
     * 设置返回代码
     * @param code
     * @return
     */
    public Response withCode(ResultCode code) {
        this.code = code.toString();
        return this;
    }

    /**
     * 设置错误子代码
     * @param subCode
     * @return
     */
    public Response withSubCode(UniformExceptionSubCodeDefine subCode) {
        if(subCode != null && !UniformExceptionSubCodeDefine.NA.equals(subCode)) {
            this.subCode = subCode.toString();
        } else {
            this.subCode = null;
        }
        return this;
    }

    /**
     * 设置异常，会把异常描述和异常详细信息放到返回对象中
     * @param e
     * @return
     */
    public Response withException(Exception e) {
        if(e == null) {
            return this;
        }

        if(StrUtil.isBlank(message)) {
            message = e.getMessage();
        }
        detailErrorInfo = ExceptionUtils.getStackTrace(e);
        return this;
    }

    /**
     * 设置错误描述信息
     * @param message
     * @return
     */
    public Response withMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * 设置返回对象
     * @param data
     * @return
     */
    public Response withData(Object data) {
        this.data = data;
        return this;
    }

    /**
     * 获取处理成功的标准返回对象
     * @param responseData
     * @return
     */
    public static Response success(final Object responseData) {
        return success(responseData, null);
    }

    /**
     * 获取处理成功的返回对象以及有异常的话对应的信息
     * @param data
     * @param resultMessage
     * @return
     */
    public static Response success(final Object data, final String resultMessage) {
        Response response = new Response();
        response.withCode(ResultCode.SUCCESS)
                .withData(data)
                .withMessage(resultMessage);

        return response;
    }

    /**
     * 根据异常获取标准的失败返回对象
     * @param e
     * @return
     */
    public static Response failed(final GenericException e) {
        if(e == null) {
            return unexpectedFailed("Exception object is null", null);
        }
        Response response = new Response();
        response.withCode(e.getResultCode())
                .withSubCode(e.getSubCode())
                .withException(e);
        return response;
    }

    /**
     * 根据异常获取标准的失败返回对象并附带额外的描述信息
     * @param resultMessage
     * @param e
     * @return
     */
    public static Response unexpectedFailed(final String resultMessage, final Exception e) {
        Response response = new Response();

        response.withCode(ResultCode.UNEXPECTED_FAILED)
                .withMessage(resultMessage)
                .withException(e);
        return response;
    }

}
