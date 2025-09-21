package com.dyslexia.dyslexia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VocabularyItemDto {
    private String word;
    private Integer startIndex;
    private Integer endIndex;
    private String definition;
    private String simplifiedDefinition;
    // Could be array or string in inbound; controller/service will normalize
    private Object examples;
    private String difficultyLevel;
    private String reason;
    private Integer gradeLevel;
    // phonemeAnalysis may be an object; will be serialized to JSON string
    private Object phonemeAnalysis;
}

