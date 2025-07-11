package com.dyslexia.dyslexia.exception;

import static com.dyslexia.dyslexia.exception.ExceptionCode.INTERNAL_SERVER_ERROR;

import com.dyslexia.dyslexia.dto.CommonResponse;
import com.dyslexia.dyslexia.exception.ApplicationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    protected ResponseEntity<CommonResponse<Void>> handleApplicationException(ApplicationException e, HttpServletRequest request){

        applicationLogFormat(e, request);

        return ResponseEntity.status(e.getExceptionCode().getHttpStatus())
            .body(new CommonResponse<Void>(e.getExceptionCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception e, HttpServletRequest request){

        logFormat(e, request);

        return ResponseEntity.internalServerError()
            .body(new CommonResponse<Void>(INTERNAL_SERVER_ERROR));
    }

    private void applicationLogFormat(ApplicationException e, HttpServletRequest request) {

        log.warn(
            "\n[{} 발생]\n" +
                "exception code: {}\n" +
                "uri: {}\n" +
                "method: {}\n" +
                "message: {}\n",
            e.getExceptionCode().name(),
            e.getExceptionCode().getCode(),
            e.getMessage(),
            request.getRequestURI(),
            request.getMethod(),
            e
        );
    }

    private void logFormat(Exception e, HttpServletRequest request) {

        log.error(
            "\n[Exception 발생]\n" +
                "uri: {}\n" +
                "method: {}\n" +
                "message: {}\n",
            request.getRequestURI(),
            request.getMethod(),
            e.getMessage(),
            e
        );
    }
}
