package com.vanish.exception;

import com.vanish.common.exception.BusinessException;
import com.vanish.common.result.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理（分级：业务/参数异常 WARN 不告警，系统异常 ERROR）
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResultVO<Void> handleBusinessException(BusinessException e) {
        log.warn("GlobalExceptionHandler | business | code:{} | message:{}", e.getCode(), e.getMessage());
        return ResultVO.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常（@RequestBody @Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultVO<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("参数校验失败");
        log.warn("GlobalExceptionHandler | invalidArgument | message:{}", message);
        return ResultVO.fail(400, message);
    }

    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResultVO<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("参数校验失败");
        log.warn("GlobalExceptionHandler | bind | message:{}", message);
        return ResultVO.fail(400, message);
    }

    /**
     * 参数校验异常（@RequestParam / @PathVariable 上的约束）
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResultVO<Void> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        log.warn("GlobalExceptionHandler | methodValidation | message:{}", e.getMessage());
        return ResultVO.fail(400, "参数校验失败");
    }

    /**
     * 404（静态资源/路径不存在）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResultVO<Void> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("GlobalExceptionHandler | notFound | uri:{}", e.getResourcePath());
        return ResultVO.fail(404, "接口不存在");
    }

    /**
     * 系统异常兜底
     */
    @ExceptionHandler(Exception.class)
    public ResultVO<Void> handleException(Exception e) {
        log.error("GlobalExceptionHandler | system | error:", e);
        return ResultVO.fail(500, "系统繁忙，请稍后重试");
    }
}
