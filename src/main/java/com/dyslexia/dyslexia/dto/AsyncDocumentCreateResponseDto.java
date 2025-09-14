package com.dyslexia.dyslexia.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AsyncDocumentCreateResponseDto {
    private String jobId;
    private String message;
}