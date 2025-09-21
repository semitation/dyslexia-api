package com.dyslexia.dyslexia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VocabularyCompleteRequestDto {
    private Integer payloadVersion;
    private String resultType; // e.g., "VOCABULARY"

    private String jobId;
    private Long textbookId;
    private String pdfName;

    // Pointer-based (preferred)
    private String s3SummaryUrl;
    private String s3BlocksPrefix;
    private Object stats; // optional

    // Inline (small docs)
    private Object summary; // optional
    private List<BlockVocabularyResultDto> blocks; // optional

    private String createdAt; // ISO-8601
}

