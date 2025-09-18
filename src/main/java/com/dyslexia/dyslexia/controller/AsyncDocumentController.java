package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.AsyncDocumentCreateResponseDto;
import com.dyslexia.dyslexia.dto.DocumentProcessingStatusDto;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.repository.GuardianRepository;
import com.dyslexia.dyslexia.service.AsyncDocumentProcessingService;
import com.dyslexia.dyslexia.util.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/v1/documents")
@RequiredArgsConstructor
@Tag(name = "비동기 교안 생성", description = "비동기 교안 생성 및 상태 조회 API")
public class AsyncDocumentController {

    private final AsyncDocumentProcessingService asyncDocumentProcessingService;
    private final JwtTokenProvider jwtTokenProvider;
    private final GuardianRepository guardianRepository;

    @PostMapping
    @Operation(summary = "비동기 교안 생성 요청",
               description = "PDF 파일을 업로드하여 비동기 교안 생성을 시작합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "교안 생성 요청이 정상적으로 접수됨"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (파일 없음, 잘못된 형식 등)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<AsyncDocumentCreateResponseDto> createDocumentAsync(
            @Parameter(description = "업로드할 PDF 파일", required = true)
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 필요합니다.");
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("PDF 파일만 업로드 가능합니다.");
        }

        Guardian guardian = getGuardianFromToken(request);

        AsyncDocumentCreateResponseDto response =
                asyncDocumentProcessingService.processDocumentAsync(guardian, file);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{jobId}/status")
    @Operation(summary = "교안 생성 상태 조회",
               description = "JobId를 통해 교안 생성 작업의 현재 상태와 진행률을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 JobId를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    public ResponseEntity<DocumentProcessingStatusDto> getDocumentStatus(
            @Parameter(description = "작업 ID", required = true)
            @PathVariable("jobId") String jobId,
            HttpServletRequest request) {

        Guardian guardian = getGuardianFromToken(request);
        DocumentProcessingStatusDto status = asyncDocumentProcessingService.getProcessingStatus(jobId);

        return ResponseEntity.ok(status);
    }

    private Guardian getGuardianFromToken(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        String clientId = jwtTokenProvider.getClientId(token);
        return guardianRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
    }
}
