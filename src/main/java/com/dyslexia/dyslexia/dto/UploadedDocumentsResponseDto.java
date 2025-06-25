package com.dyslexia.dyslexia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "업로드한 문서 목록 응답 DTO")
public class UploadedDocumentsResponseDto {

  @Schema(description = "처리 성공 여부", example = "true")
  private boolean success;

  @Schema(description = "응답 메시지", example = "업로드된 문서 목록 조회 성공")
  private String message;

  @Schema(description = "문서 목록")
  private List<DocumentDto> documents;
} 