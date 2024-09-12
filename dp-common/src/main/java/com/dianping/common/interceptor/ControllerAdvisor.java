package com.dianping.common.interceptor;

import com.dianping.common.utils.GenericException;
import com.dianping.common.utils.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 用户统一来处理Restful返回的结果，主要对于非标准体的错误，包装成标准体
 * com.hellobike.pmo.cockpit.web.framework.Response
 */
@RestControllerAdvice
@Slf4j
public class ControllerAdvisor implements ResponseBodyAdvice<Object> {

    /**
     * 目前默认所有返回都要包装
     * @param returnType
     * @param converterType
     * @return
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * 处理所有返回对象，包装成前端可识别的标准返回对象
     * com.hellobike.pmo.cockpit.web.framework.Respose
     *
     * @param o
     * @param methodParameter
     * @param mediaType
     * @param aClass
     * @param serverHttpRequest
     * @param serverHttpResponse
     * @return
     */
    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if(null == o){
            return Response.success(o);
        }


        // 如果已经是标准体，则直接返回
        if(o instanceof Response) {
            return o;
        }



        // 先判断返回对象是否是系统应用级的错误，是的话包装成标准的错误返回对象
        // TODO: 当前只处理了404，服务不存在的错误，还有其他应用级错误，发现后应该补充在这。
        if(o instanceof LinkedHashMap) {
            if(((LinkedHashMap)o).containsKey("status")) {
                if("404".equals(String.valueOf(((LinkedHashMap)o).get("status")))) {
                    Response response = Response.unexpectedFailed("Requested API service not found!", null);
                    response.setData(o);
                    return response;
                }
            }
        }
        return Response.success(o);
    }

    /**
     * 对最终有异常抛出的情况，把异常包装成标准的错误返回对象
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler
    public Response handleException(final HttpServletRequest request, final HttpServletResponse response, final Exception e) {
        //log.error("程序异常,请求url:{},异常信息{}",request.getRequestURI(),e);
        if(e instanceof GenericException) {
            return Response.failed((GenericException) e);
        } else if(e instanceof MethodArgumentNotValidException ){
            MethodArgumentNotValidException methodArgumentNotValidException =((MethodArgumentNotValidException)e);
            List<ObjectError> allErrors = methodArgumentNotValidException.getBindingResult().getAllErrors();
            StringBuilder message = new StringBuilder();
            allErrors.forEach(error -> message.append(error.getDefaultMessage()).append(";"));
            return Response.unexpectedFailed(message.toString(),e);
        } else {
            return Response.unexpectedFailed(e.getMessage(), e);
        }
    }
}
