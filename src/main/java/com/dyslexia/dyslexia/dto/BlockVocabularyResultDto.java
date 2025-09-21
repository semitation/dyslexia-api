package com.dyslexia.dyslexia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockVocabularyResultDto {
    private String jobId;
    private Long textbookId;
    private Integer pageNumber;
    private String blockId;
    private String originalSentence;
    private List<VocabularyItemDto> vocabularyItems;
    private String createdAt; // ISO-8601 string; service will parse safely
}

