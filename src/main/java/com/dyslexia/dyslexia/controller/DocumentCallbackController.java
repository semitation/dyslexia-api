package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.DocumentCompleteAckDto;
import com.dyslexia.dyslexia.dto.DocumentCompleteRequestDto;
import com.dyslexia.dyslexia.repository.DocumentRepository;
import com.dyslexia.dyslexia.service.DocumentCallbackService;
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
@RequestMapping(path = "/document", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Document Callback", description = "FastAPI가 PDF 변환 완료 시 호출하는 콜백")
public class DocumentCallbackController {

    private final DocumentCallbackService documentCallbackService;
    private final DocumentRepository documentRepository;

    @Value("${external.callback.token:}")
    private String callbackToken;

    @PostMapping(path = "/complete", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "문서 처리 완료 콜백", description = "FastAPI가 작업 완료 시 결과 JSON을 전송합니다.")
    public ResponseEntity<?> complete(
            @Valid @RequestBody DocumentCompleteRequestDto requestDto,
            @RequestHeader(value = "X-Callback-Token", required = false) String token,
            HttpServletRequest httpRequest) {

        long start = System.currentTimeMillis();

        // Optional header-based auth
        if (callbackAuthEnabled() && (token == null || token.isBlank() || !token.equals(callbackToken))) {
            log.warn("콜백 인증 실패. IP: {}, JobId: {}", httpRequest.getRemoteAddr(), requestDto.getJobId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(error("invalid callback token"));
        }

        if (requestDto.getData() == null || !requestDto.getData().isObject()) {
            return ResponseEntity.badRequest().body(error("'data' must be a JSON object"));
        }

        int payloadBytes = requestDto.getData().toString().getBytes().length;
        log.info("/document/complete 수신 - jobId: {}, pdfName: {}, payloadBytes: {}", requestDto.getJobId(), requestDto.getPdfName(), payloadBytes);

        // If the jobId is unknown to our system, treat as 422
        if (documentRepository.findByJobId(requestDto.getJobId()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(error("unknown jobId"));
        }
        try {
            boolean created = documentCallbackService.handleCompletion(requestDto);
            long tookMs = System.currentTimeMillis() - start;
            log.info("콜백 처리 응답 - jobId: {}, duplicate: {}, tookMs: {}", requestDto.getJobId(), !created, tookMs);
            return ResponseEntity.ok(new DocumentCompleteAckDto(true, requestDto.getJobId()));
        } catch (IllegalArgumentException e) {
            // Fallback for explicit validation exceptions
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(e.getMessage()));
        }
    }

    private boolean callbackAuthEnabled() {
        return callbackToken != null && !callbackToken.isBlank();
    }

    private static Object error(String message) {
        return java.util.Map.of("success", false, "error", message);
    }
}
