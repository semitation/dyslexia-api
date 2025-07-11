package com.dyslexia.dyslexia.dto;

import com.dyslexia.dyslexia.exception.ExceptionCode;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record CommonResponse<T>(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp,
    int code,
    String message,
    T result
) {

    // api 응답이 성공이고, 반환값 있는 경우
    public CommonResponse(String message, T result) {
        this(LocalDateTime.now(), 1000, message, result);
    }

    // api 응답이 성공이고, 반환값 없는 경우
    public CommonResponse(String message) {
        this(LocalDateTime.now(), 1000, message, null);
    }

    // api 응답이 실패인 경우
    public CommonResponse(ExceptionCode exceptionCode) {
        this(LocalDateTime.now(), exceptionCode.getCode(), exceptionCode.getMessage(), null);
    }
}