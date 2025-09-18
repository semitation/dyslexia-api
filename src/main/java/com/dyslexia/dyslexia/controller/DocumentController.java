package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.CommonResponse;
import com.dyslexia.dyslexia.dto.DocumentCompleteAckDto;
import com.dyslexia.dyslexia.dto.DocumentCompleteRequestDto;
import com.dyslexia.dyslexia.dto.DocumentDto;
import com.dyslexia.dyslexia.exception.ExceptionCode;
import com.dyslexia.dyslexia.repository.DocumentRepository;
import com.dyslexia.dyslexia.service.ConvertProcessService;
import com.dyslexia.dyslexia.service.DocumentCallbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Document", description = "PDF 문서 관리 API")
@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

  private final ConvertProcessService convertProcessService;
  private final DocumentCallbackService documentCallbackService;
  private final DocumentRepository documentRepository;

  @Value("${external.callback.token:}")
  private String callbackToken;

  @Operation(summary = "PDF 문서 업로드", description = "보호자가 PDF 문서를 업로드하고 처리를 시작합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "업로드 성공",
          content = @Content(schema = @Schema(implementation = DocumentDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping(value = "/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<CommonResponse<DocumentDto>> uploadDocument(
      @Parameter(description = "보호자 ID", required = true) @RequestParam("guardianId") Long guardianId,
      @Parameter(description = "PDF 파일", required = true) @RequestParam("file") MultipartFile file,
      @Parameter(description = "문서 제목", required = true) @RequestParam("title") String title
  ) throws IOException {
    log.info("문서 업로드 요청: 보호자({}), 문서 제목: {}", guardianId, title);

    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
      return ResponseEntity.badRequest().body(
          new CommonResponse<DocumentDto>(ExceptionCode.INVALID_FILE_TYPE)
      );
    }

    DocumentDto dto = convertProcessService.uploadDocument(guardianId, file, title);

    return ResponseEntity.ok(new CommonResponse<>("PDF 업로드 완료. 비동기 처리가 시작되었습니다.", dto));
  }

  @PostMapping(path = "/document/complete", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "문서 처리 완료 콜백", description = "FastAPI가 작업 완료 시 결과 JSON을 전송합니다.")
  public ResponseEntity<?> complete(
      @Valid @RequestBody DocumentCompleteRequestDto requestDto,
      @RequestHeader(value = "X-Callback-Token", required = false) String token,
      HttpServletRequest httpRequest
  ) {
    long start = System.currentTimeMillis();

    if (callbackAuthEnabled() && (token == null || token.isBlank() || !token.equals(callbackToken))) {
      log.warn("콜백 인증 실패. IP: {}, JobId: {}", httpRequest.getRemoteAddr(), requestDto.getJobId());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("invalid callback token"));
    }

    if (requestDto.getData() == null || !requestDto.getData().isObject()) {
      return ResponseEntity.badRequest().body(error("'data' must be a JSON object"));
    }

    int payloadBytes = requestDto.getData().toString().getBytes().length;
    log.info("/document/complete 수신 - jobId: {}, pdfName: {}, payloadBytes: {}", requestDto.getJobId(), requestDto.getPdfName(), payloadBytes);

    if (documentRepository.findByJobId(requestDto.getJobId()).isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error("unknown jobId"));
    }

    try {
      boolean created = documentCallbackService.handleCompletion(requestDto);
      long tookMs = System.currentTimeMillis() - start;
      log.info("콜백 처리 응답 - jobId: {}, duplicate: {}, tookMs: {}", requestDto.getJobId(), !created, tookMs);
      return ResponseEntity.ok(new DocumentCompleteAckDto(true, requestDto.getJobId()));
    } catch (IllegalArgumentException e) {
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
