package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.CommonResponse;
import com.dyslexia.dyslexia.dto.DocumentDto;
import com.dyslexia.dyslexia.dto.UploadedDocumentsResponseDto;
import com.dyslexia.dyslexia.entity.Document;
import com.dyslexia.dyslexia.exception.ApplicationException;
import com.dyslexia.dyslexia.exception.ExceptionCode;
import com.dyslexia.dyslexia.mapper.DocumentMapper;
import com.dyslexia.dyslexia.repository.DocumentRepository;
import com.dyslexia.dyslexia.repository.GuardianRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "GuardianDocument", description = "보호자 문서 관리 API")
@RestController
@RequestMapping("guardians/{guardianId}")
@RequiredArgsConstructor
@Slf4j
public class GuardianDocumentController {

  private final DocumentRepository documentRepository;
  private final GuardianRepository guardianRepository;
  DocumentMapper documentMapper;

  @Operation(summary = "보호자가 업로드한 문서 목록 조회", description = "보호자가 업로드한 모든 문서 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = UploadedDocumentsResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "보호자를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping("documents")
  public ResponseEntity<CommonResponse<List<DocumentDto>>> getGuardianDocuments(
      @Parameter(description = "보호자 ID", required = true)
      @PathVariable("guardianId") Long guardianId) {

    log.info("보호자({})의 문서 목록 조회 요청", guardianId);

    // 존재 여부 체크
    guardianRepository.findById(guardianId)
        .orElseThrow(() -> new ApplicationException(ExceptionCode.GUARDIAN_NOT_FOUND));

    List<Document> documents = documentRepository.findByGuardianIdOrderByUploadedAtDesc(guardianId);

    List<DocumentDto> dtos = documents.stream()
        .map(documentMapper::toDto)
        .toList();

    return ResponseEntity.ok(new CommonResponse<>(
        documents.isEmpty() ? "업로드한 문서가 없습니다." : "문서 목록 조회 성공",
        dtos
    ));
  }
}
