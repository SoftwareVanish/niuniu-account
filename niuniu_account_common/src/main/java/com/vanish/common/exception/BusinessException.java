package com.vanish.common.exception;

import lombok.Getter;

/**
 * 业务异常（业务校验不通过时抛出，不触发系统级告警）
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
