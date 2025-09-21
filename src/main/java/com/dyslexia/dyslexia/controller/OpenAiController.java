package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.OpenAiSpeechRequestDto;
import com.dyslexia.dyslexia.service.OpenAiSpeechService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "OpenAI", description = "OpenAI 통합 API")
@RequestMapping("")
public class OpenAiController {

    private final OpenAiSpeechService openAiSpeechService;

    @PostMapping(value = "/openai/speech", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "텍스트 → 음성 변환", description = "OpenAI gpt-4o-mini-tts 등을 호출하여 음성 데이터를 반환합니다.")
    public ResponseEntity<byte[]> speech(@Valid @RequestBody OpenAiSpeechRequestDto request) {
        try {
            var result = openAiSpeechService.synthesize(request);
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, result.contentType());
            headers.setContentDisposition(ContentDisposition.attachment().filename(result.filename()).build());
            return new ResponseEntity<>(result.data(), headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("/openai/speech 처리 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

