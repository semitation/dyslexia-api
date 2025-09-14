package com.dyslexia.dyslexia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FailureMessageDto {
    private String jobId;
    private String error;
}