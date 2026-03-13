package com.alian.emci.exception;

import com.alian.emci.common.Result;
import com.alian.emci.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {}, URL: {}", e.getMessage(), request.getRequestURI());
        return Result.<Void>error(e.getCode(), e.getMessage())
                .path(request.getRequestURI());
    }

    /**
     * 参数校验异常 - @Valid @RequestBody
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        String message = String.join(", ", errors);
        log.warn("参数校验失败: {}, URL: {}", message, request.getRequestURI());

        return Result.<Void>error(ResultCode.VALIDATION_ERROR.getCode(), message)
                .path(request.getRequestURI());
    }

    /**
     * 参数校验异常 - @Validated
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e, HttpServletRequest request) {
        List<String> errors = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        String message = String.join(", ", errors);
        log.warn("参数校验失败: {}, URL: {}", message, request.getRequestURI());

        return Result.<Void>error(ResultCode.VALIDATION_ERROR.getCode(), message)
                .path(request.getRequestURI());
    }

    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        List<String> errors = e.getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        String message = String.join(", ", errors);
        log.warn("参数绑定失败: {}, URL: {}", message, request.getRequestURI());

        return Result.<Void>error(ResultCode.BAD_REQUEST.getCode(), message)
                .path(request.getRequestURI());
    }

    /**
     * 认证异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleBadCredentials(BadCredentialsException e, HttpServletRequest request) {
        log.warn("认证失败: {}, URL: {}", e.getMessage(), request.getRequestURI());
        return Result.<Void>error(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误")
                .path(request.getRequestURI());
    }

    /**
     * 权限不足
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        log.warn("权限不足: {}, URL: {}", e.getMessage(), request.getRequestURI());
        return Result.<Void>error(ResultCode.FORBIDDEN)
                .path(request.getRequestURI());
    }

    /**
     * 文件过大
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("文件过大: {}, URL: {}", e.getMessage(), request.getRequestURI());
        return Result.<Void>error(ResultCode.FILE_TOO_LARGE)
                .path(request.getRequestURI());
    }

    /**
     * 其他所有异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {}, URL: {}", e.getMessage(), request.getRequestURI(), e);
        return Result.<Void>error(ResultCode.ERROR.getCode(), "系统繁忙，请稍后重试")
                .path(request.getRequestURI());
    }
}
