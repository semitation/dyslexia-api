package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.CommonResponse;
import com.dyslexia.dyslexia.dto.DocumentDto;
import com.dyslexia.dyslexia.dto.PageContentResponseDto;
import com.dyslexia.dyslexia.service.DocumentContentService;
import com.dyslexia.dyslexia.service.GuardianDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "GuardianDocument", description = "보호자 문서 관리 API")
@RestController
@RequestMapping("/guardian/documents")
@RequiredArgsConstructor
@Slf4j
public class GuardianDocumentController {

  private final GuardianDocumentService guardianDocumentService;
  private final DocumentContentService documentContentService;

  @Operation(summary = "내 문서 목록 조회", description = "현재 인증된 보호자가 업로드한 모든 문서 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = DocumentDto.class))),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping
  public ResponseEntity<CommonResponse<List<DocumentDto>>> getMyDocuments() {
    log.info("내 문서 목록 조회 요청");

    List<DocumentDto> documents = guardianDocumentService.getMyDocuments();

    return ResponseEntity.ok(new CommonResponse<>(
        documents.isEmpty() ? "업로드한 문서가 없습니다." : "문서 목록 조회 성공",
        documents
    ));
  }

  @Operation(
      summary = "내 문서 페이지 조회",
      description = "문서 ID로 페이지 콘텐츠(JSON)를 조회합니다. page를 지정하면 특정 페이지만 반환합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = PageContentResponseDto.class))),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "문서 접근 권한 없음"),
      @ApiResponse(responseCode = "404", description = "문서를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping("/{documentId}/pages")
  public ResponseEntity<CommonResponse<List<PageContentResponseDto>>> getMyDocumentPages(
      @PathVariable("documentId") Long documentId,
      @RequestParam(name = "page", required = false) Integer pageNumber) {

    log.info("내 문서 페이지 조회 요청: documentId={}, page={}", documentId, pageNumber);

    List<PageContentResponseDto> pages = documentContentService.getMyDocumentPages(documentId, pageNumber);

    return ResponseEntity.ok(new CommonResponse<>(
        pages.isEmpty() ? "페이지가 없습니다." : "페이지 조회 성공",
        pages
    ));
  }
}
