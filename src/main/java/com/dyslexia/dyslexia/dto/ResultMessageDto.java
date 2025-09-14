package com.dyslexia.dyslexia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResultMessageDto {
    private String jobId;
    private String s3Url;
}