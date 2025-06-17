package com.dyslexia.dyslexia.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyAnalysisSearchRequestDto {
    private Long documentId;
    private Integer pageNumber;
    private String blockId;
} 