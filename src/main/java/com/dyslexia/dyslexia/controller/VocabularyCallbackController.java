package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.DocumentCompleteAckDto;
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
@Tag(name = "AI Callback", description = "어휘 분석 완료 콜백 수신")
@RequestMapping("")
public class VocabularyCallbackController {

    private final VocabularyCallbackService vocabularyCallbackService;

    @Value("${external.callback.token:}")
    private String callbackToken;

    @PostMapping(path = "/v1/ai/vocabulary/complete", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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

