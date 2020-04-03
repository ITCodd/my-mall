package com.github.advice;

import com.github.utils.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 方法参数校验
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public JsonData handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        return JsonData.fail(400,e.getBindingResult().getFieldError().getDefaultMessage());
    }

    @ExceptionHandler(BindException.class)
    public JsonData handleMethodArgumentNotValidException(BindException e) {
        log.error(e.getMessage(), e);
        JsonData jsonData = JsonData.fail(400, e.getBindingResult().getFieldError().getDefaultMessage());
        return jsonData;
    }

    @ExceptionHandler(Exception.class)
    public JsonData handleMethodArgumentNotValidException(Exception e) {
        log.error(e.getMessage(), e);
        return JsonData.fail(e.getMessage());
    }
}
