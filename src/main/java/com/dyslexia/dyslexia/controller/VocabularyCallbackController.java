package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.DocumentCompleteAckDto;
import com.dyslexia.dyslexia.dto.VocabularyBlockRequestDto;
import com.dyslexia.dyslexia.dto.VocabularyCompleteRequestDto;
import com.dyslexia.dyslexia.service.VocabularyCallbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "AI Callback", description = "어휘 분석 콜백 수신 API")
@RequestMapping("/v1/ai/vocabulary")
public class VocabularyCallbackController {

    private final VocabularyCallbackService vocabularyCallbackService;

    @Value("${external.callback.token:deveungi}")
    private String callbackToken;

    @PostMapping(path = "/block", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "블록 단위 어휘 분석 콜백", description = "FastAPI에서 블록별로 어휘 분석 결과를 전송받아 DB에 저장합니다.")
    public ResponseEntity<?> handleBlock(
        @Valid @RequestBody VocabularyBlockRequestDto requestDto,
        @RequestHeader(value = "X-Callback-Token", required = false) String token,
        @RequestHeader(value = "X-Request-Id", required = false) String requestId,
        @RequestHeader(value = "X-Trace-Id", required = false) String traceId,
        HttpServletRequest httpRequest
    ) {
        long start = System.currentTimeMillis();

        // 토큰 검증
        if (callbackAuthEnabled() && (token == null || token.isBlank() || !token.equals(callbackToken))) {
            log.warn("블록 콜백 인증 실패. IP: {}, jobId: {}", httpRequest.getRemoteAddr(), requestDto.getJobId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(java.util.Map.of("success", false, "error_code", "UNAUTHORIZED", "message", "invalid token"));
        }

        // 필수 필드 검증
        if (requestDto.getJobId() == null || requestDto.getJobId().isBlank()) {
            return ResponseEntity.badRequest()
                .body(java.util.Map.of("success", false, "error_code", "MISSING_JOB_ID", "message", "'job_id' is required"));
        }
        if (requestDto.getTextbookId() == null) {
            return ResponseEntity.badRequest()
                .body(java.util.Map.of("success", false, "error_code", "MISSING_TEXTBOOK_ID", "message", "'textbook_id' is required"));
        }
        if (requestDto.getBlockId() == null || requestDto.getBlockId().isBlank()) {
            return ResponseEntity.badRequest()
                .body(java.util.Map.of("success", false, "error_code", "MISSING_BLOCK_ID", "message", "'block_id' is required"));
        }

        // vocabulary_items 길이 검증 (1-5개)
        if (requestDto.getVocabularyItems() == null || requestDto.getVocabularyItems().isEmpty() || requestDto.getVocabularyItems().size() > 5) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(java.util.Map.of("success", false, "error_code", "INVALID_ITEMS_COUNT", "message", "vocabulary_items must be 1..5"));
        }

        // 요청 상세 로그
        log.info("=== FastAPI 블록 콜백 수신 ===");
        log.info("요청 헤더 - X-Callback-Token: {}, X-Request-Id: {}, X-Trace-Id: {}",
            token != null ? "***" : "null", requestId, traceId);
        log.info("요청 본문 - jobId: {}, textbookId: {}, blockId: {}, pageNumber: {}",
            requestDto.getJobId(), requestDto.getTextbookId(), requestDto.getBlockId(), requestDto.getPageNumber());
        log.info("어휘 항목 수: {}, 원본 문장: {}",
            requestDto.getVocabularyItems().size(), requestDto.getOriginalSentence());

        // 음운 분석 정보 사전 확인
        long phonemeItemCount = requestDto.getVocabularyItems().stream()
            .mapToLong(item -> (item.getPhonemeAnalysisJson() != null && !item.getPhonemeAnalysisJson().isBlank()) ||
                             item.getPhonemeAnalysis() != null ? 1L : 0L)
            .sum();
        log.info("음운 분석 포함된 어휘 항목: {}/{}", phonemeItemCount, requestDto.getVocabularyItems().size());

        int saved;
        try {
            saved = vocabularyCallbackService.handleBlockCallback(requestDto);
            log.info("블록 콜백 처리 성공 - 저장된 항목 수: {}", saved);
        } catch (Exception e) {
            log.error("=== 블록 콜백 처리 실패 ===");
            log.error("jobId: {}, blockId: {}, error: {}", requestDto.getJobId(), requestDto.getBlockId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("success", false, "error_code", "PROCESSING_FAILED", "message", "processing failed"));
        }

        long took = System.currentTimeMillis() - start;

        // 성공 응답 생성
        java.util.Map<String, Object> response = java.util.Map.of(
            "success", true,
            "jobId", requestDto.getJobId(),
            "blockId", requestDto.getBlockId(),
            "saved", saved
        );

        log.info("=== 블록 콜백 응답 전송 ===");
        log.info("응답 상태: 200 OK");
        log.info("응답 본문: {}", response);
        log.info("처리 시간: {}ms", took);

        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/complete", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "어휘 분석 완료 콜백", description = "FastAPI가 어휘 분석 최종 결과(S3 포인터 또는 인라인)를 전송합니다.")
    public ResponseEntity<?> complete(
        @Valid @RequestBody VocabularyCompleteRequestDto requestDto,
        @RequestHeader(value = "X-Callback-Token", required = false) String token,
        @RequestHeader(value = "X-Request-Id", required = false) String requestId,
        @RequestHeader(value = "X-Trace-Id", required = false) String traceId,
        HttpServletRequest httpRequest
    ) {
        long start = System.currentTimeMillis();

        if (callbackAuthEnabled() && (token == null || token.isBlank() || !token.equals(callbackToken))) {
            log.warn("어휘 콜백 인증 실패. IP: {}, jobId: {}", httpRequest.getRemoteAddr(), requestDto.getJobId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("invalid callback token"));
        }

        if (requestDto.getJobId() == null || requestDto.getJobId().isBlank()) {
            return ResponseEntity.badRequest().body(error("'job_id' is required"));
        }
        if (requestDto.getTextbookId() == null) {
            return ResponseEntity.badRequest().body(error("'textbook_id' is required"));
        }

        int saved;
        try {
            log.info("/v1/ai/vocabulary/complete 수신 - jobId: {}, textbookId: {}, pdfName: {}", requestDto.getJobId(), requestDto.getTextbookId(), requestDto.getPdfName());
            saved = vocabularyCallbackService.handleCompletion(requestDto);
        } catch (Exception e) {
            log.error("어휘 분석 콜백 처리 실패 - jobId: {}", requestDto.getJobId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("processing failed"));
        }

        long took = System.currentTimeMillis() - start;
        log.info("어휘 분석 콜백 처리 완료 - jobId: {}, savedItems: {}, tookMs: {}", requestDto.getJobId(), saved, took);
        return ResponseEntity.ok(new DocumentCompleteAckDto(true, requestDto.getJobId()));
    }

    private boolean callbackAuthEnabled() {
        return callbackToken != null && !callbackToken.isBlank();
    }

    private static Object error(String message) {
        return java.util.Map.of("success", false, "error", message);
    }
}

