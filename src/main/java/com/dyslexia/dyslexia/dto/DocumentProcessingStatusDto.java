package com.dyslexia.dyslexia.dto;

import com.dyslexia.dyslexia.enums.DocumentProcessingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DocumentProcessingStatusDto {
    private String jobId;
    private String fileName;
    private DocumentProcessingStatus status;
    private Integer progress;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}