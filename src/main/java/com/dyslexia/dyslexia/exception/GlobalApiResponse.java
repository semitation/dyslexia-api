package com.dyslexia.dyslexia.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "API 오류 응답 모델")
public class GlobalApiResponse<T> {
  private boolean success;
  private String message;
  private T data;

  public GlobalApiResponse(boolean success, String message, T data) {
    this.success = success;
    this.message = message;
    this.data = data;
  }

  public static <T> GlobalApiResponse<T> ok(T data) {
    return new GlobalApiResponse<>(true, "성공", data);
  }

  public static <T> GlobalApiResponse<T> ok(String message, T data) {
    return new GlobalApiResponse<>(true, message, data);
  }

  public static <T> GlobalApiResponse<T> fail(String message) {
    return new GlobalApiResponse<>(false, message, null);
  }

  public static <T> GlobalApiResponse<T> fail(String message, T data) {
    return new GlobalApiResponse<>(false, message, data);
  }
}
