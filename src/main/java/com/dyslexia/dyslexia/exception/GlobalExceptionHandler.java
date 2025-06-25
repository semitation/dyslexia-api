package com.dyslexia.dyslexia.exception;

import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<GlobalApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
    return ResponseEntity.status(403).body(
        GlobalApiResponse.fail(ex.getMessage())
    );
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<GlobalApiResponse<Void>> handleNotFound(EntityNotFoundException ex) {
    return ResponseEntity.status(404).body(
        GlobalApiResponse.fail(ex.getMessage())
    );
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<GlobalApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
    return ResponseEntity.status(404).body(
        GlobalApiResponse.fail(ex.getMessage())
    );
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<GlobalApiResponse<Void>> handleGeneralError(Exception ex) {
    return ResponseEntity.status(500).body(
        GlobalApiResponse.fail("오류가 발생했습니다: " + ex.getMessage())
    );
  }

  @ExceptionHandler(IOException.class)
  public ResponseEntity<GlobalApiResponse<Void>> handleFileUploadError(IOException ex) {
    return ResponseEntity.status(500).body(
        GlobalApiResponse.fail("파일 업로드 중 오류가 발생했습니다: " + ex.getMessage())
    );
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<GlobalApiResponse<Void>> handleRuntime(RuntimeException ex) {
    return ResponseEntity.status(500).body(
        GlobalApiResponse.fail(ex.getMessage())
    );
  }

  /*@ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    StringBuilder sb = new StringBuilder();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      sb.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ");
    }
    log.warn("Validation failed: {}", sb);
    return buildErrorResponse(HttpStatus.BAD_REQUEST, sb.toString());
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiErrorResponse> handleStatus(ResponseStatusException ex) {
    log.warn("StatusException: {}", ex.getMessage());
    return buildErrorResponse((HttpStatus) ex.getStatusCode(), ex.getReason());
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ApiErrorResponse> handleRuntime(RuntimeException ex) {
    log.error("Unhandled RuntimeException", ex);
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
  }

  @ExceptionHandler(IOException.class)
  public ResponseEntity<ApiErrorResponse> handleIO(IOException ex) {
    log.error("IO Exception", ex);
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
    log.error("Unexpected Exception", ex);
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다.");
  }

  @ExceptionHandler(GuardianNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleGuardianNotFoundException(
      GuardianNotFoundException ex) {
    log.warn("Guardian not found: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(StudentNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleStudentNotFoundException(
      StudentNotFoundException ex) {
    log.warn("Student not found: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ApiErrorResponse> handleUserAlreadyExistsException(
      UserAlreadyExistsException ex) {
    log.warn("Guardian already exists: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
  }*/
}