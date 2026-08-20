package com.vanish.common.result;

import lombok.Data;

/**
 * 统一响应结果
 */
@Data
public class ResultVO<T> {

    /** 响应码：200 成功 */
    private int code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    /**
     * 成功（无数据）
     */
    public static <T> ResultVO<T> success() {
        ResultVO<T> vo = new ResultVO<>();
        vo.setCode(200);
        vo.setMessage("success");
        return vo;
    }

    /**
     * 成功（带数据）
     */
    public static <T> ResultVO<T> successWithData(T data) {
        ResultVO<T> vo = new ResultVO<>();
        vo.setCode(200);
        vo.setMessage("success");
        vo.setData(data);
        return vo;
    }

    /**
     * 成功（带提示消息）
     */
    public static <T> ResultVO<T> successWithMessage(String message) {
        ResultVO<T> vo = new ResultVO<>();
        vo.setCode(200);
        vo.setMessage(message);
        return vo;
    }

    /**
     * 失败（默认 400）
     */
    public static <T> ResultVO<T> fail(String message) {
        return fail(400, message);
    }

    /**
     * 失败（自定义响应码）
     */
    public static <T> ResultVO<T> fail(int code, String message) {
        ResultVO<T> vo = new ResultVO<>();
        vo.setCode(code);
        vo.setMessage(message);
        return vo;
    }
}
