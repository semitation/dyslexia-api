package com.dyslexia.dyslexia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VocabularyItemDto {

    @NotBlank(message = "word is required")
    private String word;

    @NotNull(message = "start_index is required")
    @Min(value = 0, message = "start_index must be >= 0")
    @JsonProperty("start_index")
    private Integer startIndex;

    @NotNull(message = "end_index is required")
    @JsonProperty("end_index")
    private Integer endIndex;

    @Size(max = 255, message = "definition must be <= 255 characters")
    private String definition;

    @Size(max = 255, message = "simplified_definition must be <= 255 characters")
    @JsonProperty("simplified_definition")
    private String simplifiedDefinition;

    // Could be array or string in inbound; controller/service will normalize
    private Object examples;

    @JsonProperty("difficulty_level")
    private String difficultyLevel;

    @Size(max = 255, message = "reason must be <= 255 characters")
    private String reason;

    @JsonProperty("grade_level")
    private Integer gradeLevel;

    // phonemeAnalysis may be an object; will be serialized to JSON string
    private Object phonemeAnalysis;

    @JsonProperty("phoneme_analysis_json")
    private String phonemeAnalysisJson;
}

