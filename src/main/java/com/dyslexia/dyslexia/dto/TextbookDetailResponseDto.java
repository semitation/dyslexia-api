package com.dyslexia.dyslexia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "교재 상세 정보 응답 DTO")
public class TextbookDetailResponseDto {

    @Schema(description = "교재 ID", example = "1")
    private Long textbookId;

    @Schema(description = "교재 이름", example = "수학 기초 교재")
    private String textbookName;

    @Schema(description = "총 페이지 수", example = "50")
    private Integer totalPages;

    @Schema(description = "할당된 학생 수", example = "3")
    private Long assignedStudentCount;

    @Schema(description = "교재 생성일자", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "변환 상태", example = "COMPLETED")
    private String convertStatus;

    @Schema(description = "원본 파일명", example = "math_textbook.pdf")
    private String originalFileName;
}
