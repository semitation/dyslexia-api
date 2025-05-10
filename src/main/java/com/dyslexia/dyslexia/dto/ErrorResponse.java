package com.dyslexia.dyslexia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "에러 응답")
public class ErrorResponse {
    @Schema(description = "에러 코드", example = "400")
    private int code;

    @Schema(description = "에러 메시지", example = "잘못된 요청입니다.")
    private String message;

    @Schema(description = "에러 상세 정보", example = "카카오 인가 코드가 유효하지 않습니다.")
    private String detail;
} 