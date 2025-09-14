package com.dyslexia.dyslexia.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AsyncDocumentProcessRequestDto {
    private String jobId;

    @Builder
    public AsyncDocumentProcessRequestDto(String jobId) {
        this.jobId = jobId;
    }
}