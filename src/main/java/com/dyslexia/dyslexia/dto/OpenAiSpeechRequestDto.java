package com.dyslexia.dyslexia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiSpeechRequestDto {
    private String text;   // required
    private String voice;  // optional (default: alloy)
    private String model;  // optional (default: gpt-4o-mini-tts)
    private String format; // optional (default: mp3) -> maps to response_format
}

