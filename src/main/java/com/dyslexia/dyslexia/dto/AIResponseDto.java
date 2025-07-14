package com.dyslexia.dyslexia.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIResponseDto {
    private String filename;
    private String text;
    private List<String> chunks;
    private AiProcessMetadataDto metadata;
} 