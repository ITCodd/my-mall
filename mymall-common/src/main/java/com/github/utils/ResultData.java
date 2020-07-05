package com.github.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ResultData<T> implements Serializable {

    private int code;

    private String message;

    private T data;


    public static <T> ResultData<T> success(T data, String msg) {
        return ResultData.<T>builder().code(200).data(data).message(msg).build();
    }

    public static <T> ResultData<T> success(T data) {
        return ResultData.<T>builder().code(200).data(data).message("OK").build();
    }

    public static ResultData success(String msg) {
        return ResultData.builder().code(200).message(msg).build();
    }

    public static ResultData<Object> success() {
        return ResultData.builder().code(200).message("OK").build();
    }


    public static <T> ResultData<T> fail(T data, String msg) {
        return ResultData.<T>builder().code(500).data(data).message(msg).build();
    }

    public static ResultData fail(String msg) {
        return ResultData.builder().code(500).message(msg).build();
    }

    public static<T> ResultData<T> fail(T data) {
        return ResultData.<T>builder().code(500).data(data).build();
    }

    public static ResultData fail(Integer httpStatus, String msg) {
        return ResultData.builder().code(httpStatus).message(msg).build();
    }

    public static  <T> ResultData<T> fail(Integer httpStatus,T data, String msg) {
        return  ResultData.<T>builder().code(httpStatus).data(data).message(msg).build();
    }

}
