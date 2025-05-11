package com.dyslexia.dyslexia.dto;

import com.dyslexia.dyslexia.entity.Page;
import com.dyslexia.dyslexia.enums.DocumentProcessStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "페이지 콘텐츠 응답")
public class PageContentResponse {

    @Schema(description = "페이지 ID")
    private Long id;

    @Schema(description = "문서 ID")
    private Long documentId;

    @Schema(description = "페이지 번호")
    private Integer pageNumber;

    @Schema(description = "원본 콘텐츠")
    private String originalContent;

    @Schema(description = "처리된 콘텐츠 (JSON)")
    private JsonNode processedContent;

    @Schema(description = "처리 상태", example = "COMPLETED")
    private DocumentProcessStatus processingStatus;

    @Schema(description = "섹션 제목")
    private String sectionTitle;

    @Schema(description = "읽기 레벨")
    private Integer readingLevel;

    @Schema(description = "단어 수")
    private Integer wordCount;

    @Schema(description = "복잡도 점수")
    private Float complexityScore;

    @Schema(description = "생성 시간")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간")
    private LocalDateTime updatedAt;

    public static PageContentResponse fromEntity(Page page) {
        return PageContentResponse.builder()
                .id(page.getId())
                .documentId(page.getDocument().getId())
                .pageNumber(page.getPageNumber())
                .originalContent(page.getOriginalContent())
                .processedContent(page.getProcessedContent())
                .processingStatus(page.getProcessingStatus())
                .sectionTitle(page.getSectionTitle())
                .readingLevel(page.getReadingLevel())
                .wordCount(page.getWordCount())
                .complexityScore(page.getComplexityScore())
                .createdAt(page.getCreatedAt())
                .updatedAt(page.getUpdatedAt())
                .build();
    }
} 