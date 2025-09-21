package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.OpenAiSpeechRequestDto;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiSpeechService {

    @Value("${ai.api.key}")
    private String openAiApiKey;

    @Value("${ai.tts.url:https://api.openai.com/v1/audio/speech}")
    private String ttsUrl;

    public SpeechResult synthesize(OpenAiSpeechRequestDto req) {
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }
        String text = safe(req.getText());
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("'text' is required");
        }
        String model = safe(req.getModel());
        if (model == null || model.isBlank()) {
            model = "gpt-4o-mini-tts";
        }
        String voice = safe(req.getVoice());
        if (voice == null || voice.isBlank()) {
            voice = "alloy";
        }
        String format = safe(req.getFormat());
        if (format == null || format.isBlank()) {
            format = "mp3";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openAiApiKey);
        headers.set("Accept", contentTypeFor(format));

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("input", text);
        body.put("voice", voice);
        // OpenAI supports either 'format' or 'response_format'; send both for safety
        body.put("format", format);
        body.put("response_format", format);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<byte[]> res = restTemplate.exchange(ttsUrl, HttpMethod.POST, entity, byte[].class);
            String contentType = res.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
            if (contentType == null) {
                contentType = contentTypeFor(format);
            }
            return new SpeechResult(res.getBody(), contentType, defaultFilename(format));
        } catch (HttpStatusCodeException e) {
            String err = e.getResponseBodyAsString(StandardCharsets.UTF_8);
            log.error("OpenAI TTS 실패: status={}, body={}", e.getStatusCode(), err);
            throw e;
        }
    }

    private static String safe(String s) {
        return s != null ? s.trim() : null;
    }

    private static String contentTypeFor(String fmt) {
        return switch (fmt.toLowerCase()) {
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "aac" -> "audio/aac";
            case "flac" -> "audio/flac";
            case "pcm" -> "audio/wave"; // sometimes application/octet-stream; keep generic
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }

    private static String defaultFilename(String fmt) {
        String ext = switch (fmt.toLowerCase()) {
            case "wav" -> "wav";
            case "aac" -> "aac";
            case "flac" -> "flac";
            case "pcm" -> "pcm";
            default -> "mp3";
        };
        return "speech." + ext;
    }

    public record SpeechResult(byte[] data, String contentType, String filename) {}
}
