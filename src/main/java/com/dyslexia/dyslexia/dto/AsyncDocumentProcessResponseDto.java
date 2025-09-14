package com.dyslexia.dyslexia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AsyncDocumentProcessResponseDto {
    private String jobId;
    private String status;
    private String message;
}