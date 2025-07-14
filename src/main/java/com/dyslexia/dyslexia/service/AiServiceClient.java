package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.AIResponseDto;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AiServiceClient {

  private final WebClient webClient;

  @Value("${ai.service.url}")
  private String aiServiceBaseUrl;

  public AiServiceClient(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.build();
  }

  public Mono<AIResponseDto> callAiPreprocess(
      MultipartFile file,
      boolean removeHeadersFooters,
      float headerHeight,
      float footerHeight,
      int maxTokens,
      boolean returnText,
      boolean returnChunks,
      String model
  ) throws IOException {
    MultipartBodyBuilder builder = new MultipartBodyBuilder();

    // 1) PDF 파일 파트
    builder
        .part("file", new InputStreamResource(file.getInputStream()))
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "form-data; name=\"file\"; filename=\"" + file.getOriginalFilename() + "\"")
        .header(HttpHeaders.CONTENT_TYPE, file.getContentType());

    // 2) 옵션 파트들 (FastAPI 기본값과 일치시키거나 필요에 따라 변경)
    builder.part("remove_headers_footers", String.valueOf(removeHeadersFooters));
    builder.part("header_height",           String.valueOf(headerHeight));
    builder.part("footer_height",           String.valueOf(footerHeight));
    builder.part("max_tokens",              String.valueOf(maxTokens));
    builder.part("return_text",             String.valueOf(returnText));
    builder.part("return_chunks",           String.valueOf(returnChunks));
    builder.part("model",                   model);

    return webClient.post()
        .uri(aiServiceBaseUrl + "/preprocess/preprocess")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(builder.build()))
        .retrieve()
        // 3) 4xx/5xx 에러 바디를 읽어서 예외로 던지기
        .onStatus(HttpStatusCode::isError, resp ->
            resp.bodyToMono(String.class)
                .flatMap(msg -> Mono.<Throwable>error(
                    new RuntimeException("AI service error: " + msg)
                ))
        )
        // 4) Map 대신 DTO 바로 언마샬
        .bodyToMono(AIResponseDto.class);
  }
}
