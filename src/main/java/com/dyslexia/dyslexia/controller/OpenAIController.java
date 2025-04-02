package com.dyslexia.dyslexia.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dyslexia.dyslexia.dto.OpenAIResponse;
import com.dyslexia.dyslexia.service.OpenAIService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/openai")
public class OpenAIController {

    private final OpenAIService openAIService;

    @PostMapping("/generate-text")
    public OpenAIResponse generateText(@RequestBody String prompt) {
        return openAIService.generateText(prompt);
    }
}
