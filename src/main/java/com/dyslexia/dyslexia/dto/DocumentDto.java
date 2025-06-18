package com.dyslexia.dyslexia.dto;

import com.dyslexia.dyslexia.enums.ConvertProcessStatus;
import com.dyslexia.dyslexia.enums.Grade;
import com.fasterxml.jackson.annotation.JsonFormat;
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
@Schema(description = "문서 정보 DTO")
public class DocumentDto {

    @Schema(description = "문서 ID", example = "1")
    private Long id;

    @Schema(description = "선생님 ID", example = "1")
    private Long teacherId;

    @Schema(description = "문서 제목", example = "생태계와 환경")
    private String title;

    @Schema(description = "원본 파일명", example = "ecology_and_environment.pdf")
    private String originalFilename;

    @Schema(description = "파일 크기(바이트)", example = "1024000")
    private Long fileSize;

    @Schema(description = "업로드 시간")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadedAt;
} 