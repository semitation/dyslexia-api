package com.dyslexia.dyslexia.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentCompleteAckDto {
    private boolean success;
    private String jobId;
}

