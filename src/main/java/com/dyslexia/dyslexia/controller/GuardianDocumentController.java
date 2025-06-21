package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.DocumentDto;
import com.dyslexia.dyslexia.dto.StudentDocumentListResponseDto;
import com.dyslexia.dyslexia.entity.Document;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.entity.StudentDocumentAssignment;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.repository.DocumentRepository;
import com.dyslexia.dyslexia.repository.StudentRepository;
import com.dyslexia.dyslexia.repository.GuardianRepository;
import com.dyslexia.dyslexia.repository.StudentTextbookAssignmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "GuardianDocument", description = "보호자 문서 관리 API")
@RestController
@RequestMapping("guardian/documents")
@RequiredArgsConstructor
@Slf4j
public class GuardianDocumentController {

    private final DocumentRepository documentRepository;

  @Operation(summary = "보호자가 업로드한 문서 목록 조회", description = "보호자가 업로드한 모든 문서 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = StudentDocumentListResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "보호자를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping("/{guardianId}")
  public ResponseEntity<StudentDocumentListResponseDto> getGuardianDocuments(
      @Parameter(description = "보호자 ID", required = true)
      @PathVariable("guardianId") Long guardianId) {

    log.info("보호자 ID: {}의 문서 목록 조회 요청", guardianId);

    try {
      List<Document> documents = documentRepository.findAllByOrderByUploadedAtDesc();

      if (documents.isEmpty()) {
        return ResponseEntity.ok(
            StudentDocumentListResponseDto.builder()
                .success(true)
                .message("업로드한 문서가 없습니다.")
                .documents(List.of())
                .build()
        );
      }

      List<DocumentDto> documentDtos = documents.stream()
          .map(document -> DocumentDto.builder()
              .id(document.getId())
              .guardianId(document.getGuardian().getId())
              .title(document.getTitle())
              .originalFilename(document.getOriginalFilename())
              .fileSize(document.getFileSize())
              .uploadedAt(document.getUploadedAt())
              .build())
          .collect(Collectors.toList());

      return ResponseEntity.ok(
          StudentDocumentListResponseDto.builder()
              .success(true)
              .message("문서 목록 조회 성공")
              .documents(documentDtos)
              .build()
      );

    } catch (Exception e) {
      log.error("보호자 문서 목록 조회 중 오류 발생", e);
      return ResponseEntity.status(500).body(
          StudentDocumentListResponseDto.builder()
              .success(false)
              .message("문서 목록 조회 중 오류가 발생했습니다: " + e.getMessage())
              .build()
      );
    }
  }
} 