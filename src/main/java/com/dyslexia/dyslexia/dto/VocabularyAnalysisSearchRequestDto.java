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
    // Accept both camelCase and snake_case
    @JsonAlias({"textbookId", "textbook_id"})
    private Long textbookId;

    @JsonAlias({"documentId", "document_id"})
    private Long documentId;

    @JsonAlias({"pageNumber", "page_number"})
    private Integer pageNumber;

    @JsonAlias({"blockId", "block_id"})
    private String blockId;
}
