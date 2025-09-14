package com.dyslexia.dyslexia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProgressMessageDto {
    private String jobId;
    private Integer progress;
}