package com.dyslexia.dyslexia.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode {

    // 1000: Success Code

    // 2000: Common Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 2000, "서버 에러가 발생하였습니다. 관리자에게 문의해 주세요."),
    BAD_REQUEST_ERROR(HttpStatus.BAD_REQUEST, 2001, "잘못된 요청입니다."),

    // 3000: Auth Error
    INVALID_SIGNUP_TOKEN(HttpStatus.UNAUTHORIZED, 3001, "유효하지 않은 가입 토큰입니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, 3002, "이미 가입된 사용자입니다."),
    OAUTH_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 3003, "외부 인증 서버와 통신 중 오류가 발생했습니다."),
    TOKEN_MISSING_AUTHORITY(HttpStatus.UNAUTHORIZED, 3004, "토큰에 권한 정보가 없습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, 3005, "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, 3006, "리프레시 토큰을 찾을 수 없습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 3007, "리프레시 토큰이 만료되었습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, 3008, "접근 권한이 없습니다."),

    // 4000: Entity Error
    GUARDIAN_NOT_FOUND(HttpStatus.NOT_FOUND, 4001, "보호자를 찾을 수 없습니다."),
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, 4002, "학생을 찾을 수 없습니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, 4003, "요청한 데이터를 찾을 수 없습니다."),

    // 5000: File Error
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 5001, "파일 업로드에 실패했습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, 5002, "파일 크기가 10MB를 초과합니다."),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, 5003, "유효하지 않은 파일명입니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, 5004, "지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif만 허용)"),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, 5005, "이미지 파일만 업로드 가능합니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, 5006, "파일을 찾을 수 없습니다."),

    // 6000: Validation Error
    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, 6001, "잘못된 인수입니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, 6002, "유효성 검사에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}
