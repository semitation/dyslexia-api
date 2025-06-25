package com.dyslexia.dyslexia.dto;

import com.dyslexia.dyslexia.enums.ConvertProcessStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "문서 정보 DTO")
public class TextbookDto {

    @Schema(description = "교재 ID", example = "1")
    private Long id;

    @Schema(description = "보호자 ID", example = "1")
    private Long guardianId;

    @Schema(description = "교재 제목", example = "생태계와 환경")
    private String title;

    @Schema(description = "페이지 수", example = "1")
    private Integer pageCount;

    @Schema(description = "학습 진행률", example = "20")
    private Integer learnRate;

    @Schema(description = "처리 상태", example = "PROCESSING")
    private ConvertProcessStatus convertProcessStatus;

    @Schema(description = "생성 시간")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "업데이트 시간")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
} 