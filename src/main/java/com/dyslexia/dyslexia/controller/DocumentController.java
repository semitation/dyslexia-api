package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.CommonResponse;
import com.dyslexia.dyslexia.dto.DocumentDto;
import com.dyslexia.dyslexia.exception.ExceptionCode;
import com.dyslexia.dyslexia.service.ConvertProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Document", description = "PDF 문서 관리 API")
@RestController
@RequestMapping("documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

  private final ConvertProcessService convertProcessService;

  @Operation(summary = "PDF 문서 업로드", description = "보호자가 PDF 문서를 업로드하고 처리를 시작합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "업로드 성공",
          content = @Content(schema = @Schema(implementation = DocumentDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
}
