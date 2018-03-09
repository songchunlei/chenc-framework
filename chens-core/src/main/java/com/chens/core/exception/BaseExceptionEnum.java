package com.chens.core.exception;

import com.chens.core.enums.IBaseEnum;

/**
 * 错误异常枚举
 * Created by songchunlei on 2018/3/8.
 */
public enum BaseExceptionEnum implements IBaseEnum {


    /**
     * token异常
     */
    TOKEN_EXPIRED(100, "token过期"),
    TOKEN_ERROR(101, "token验证失败"),

    /**
     * 登录校验异常
     */
    AUTH_REQUEST_ERROR(200, "账号密码错误"),

    /**
     * 请求异常
     */
    REQUEST_NULL(500, "请求有错误"),
    SERVER_ERROR(600, "服务器异常");

    private Integer code;

    private String message;

    BaseExceptionEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }


}
