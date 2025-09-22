package com.dyslexia.dyslexia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VocabularyBlockRequestDto {

    @NotBlank(message = "job_id is required")
    @JsonProperty("job_id")
    private String jobId;

    @NotNull(message = "textbook_id is required")
    @JsonProperty("textbook_id")
    private Long textbookId;

    @NotBlank(message = "block_id is required")
    @JsonProperty("block_id")
    private String blockId;

    @JsonProperty("page_number")
    private Integer pageNumber;

    @JsonProperty("original_sentence")
    private String originalSentence;

    @JsonProperty("created_at")
    private String createdAt;

    @NotNull(message = "vocabulary_items is required")
    @Size(min = 1, max = 5, message = "vocabulary_items must contain 1-5 items")
    @Valid
    @JsonProperty("vocabulary_items")
    private List<VocabularyItemDto> vocabularyItems;
}