package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.AsyncDocumentProcessRequestDto;
import com.dyslexia.dyslexia.dto.AsyncDocumentProcessResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class FastApiClient {

    private final RestTemplate restTemplate;

    @Value("${external.fastapi.url}")
    private String fastApiUrl;

    @Value("${external.fastapi.endpoints.process}")
    private String processEndpoint;

    public FastApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AsyncDocumentProcessResponseDto processDocumentAsync(MultipartFile file, String jobId) {
        try {
            String url = fastApiUrl + processEndpoint;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            body.add("job_id", jobId);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<AsyncDocumentProcessResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    AsyncDocumentProcessResponseDto.class
            );

            log.info("FastAPI 비동기 처리 요청 성공. JobId: {}, Status: {}",
                    jobId, response.getBody().getStatus());

            return response.getBody();

        } catch (Exception e) {
            log.error("FastAPI 비동기 처리 요청 실패. JobId: {}", jobId, e);
            throw new RuntimeException("FastAPI 서버 통신 오류", e);
        }
    }
}