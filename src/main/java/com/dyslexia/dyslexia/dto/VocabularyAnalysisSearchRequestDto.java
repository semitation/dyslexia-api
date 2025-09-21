package com.dyslexia.dyslexia.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VocabularyAnalysisSearchRequestDto {
    @JsonAlias({"documentId", "document_id", "textbook_id"})
    private Long textbookId;
    private Integer pageNumber;
    private String blockId;
}
