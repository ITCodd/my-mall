package com.github.utils;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JsonData<T> {

    private int code;

    private String message;

    private T data;


    public static <T> JsonData<T> success(T data, String msg) {
        return JsonData.<T>builder().code(200).data(data).message(msg).build();
    }

    public static <T> JsonData<T> success(T data) {
        return JsonData.<T>builder().code(200).data(data).message("OK").build();
    }

    public static JsonData successMsg(String msg) {
        return JsonData.builder().code(200).message(msg).build();
    }

    public static JsonData<Object> success() {
        return JsonData.builder().code(200).message("OK").build();
    }


    public static <T> JsonData<T> fail(T data, String msg) {
        return JsonData.<T>builder().code(500).data(data).message(msg).build();
    }

    public static JsonData<Object> fail(String msg) {
        return JsonData.builder().code(500).message(msg).build();
    }


    public static JsonData fail(Integer httpStatus, String msg) {
        return JsonData.builder().code(httpStatus).message(msg).build();
    }

}
