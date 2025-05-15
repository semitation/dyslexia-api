package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.config.ReplicateConfig;
import com.dyslexia.dyslexia.domain.pdf.Block;
import com.dyslexia.dyslexia.domain.pdf.BlockImpl;
import com.dyslexia.dyslexia.enums.Grade;
import com.dyslexia.dyslexia.enums.ImageType;
import com.dyslexia.dyslexia.enums.TermType;
import com.dyslexia.dyslexia.util.ChatRequestBuilder;
import com.dyslexia.dyslexia.util.PromptBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dyslexia.dyslexia.util.DocumentProcessHolder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIPromptService {

  private static final String MODEL = "gpt-4o-mini";
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  @Value("${ai.api.url}")
  private String aiApiUrl;
  @Value("${ai.api.key}")
  private String aiApiKey;

  private final ReplicateConfig replicateConfig;
  @Value("${app.upload.dir:uploads}")
  private String uploadDir;

  private Map<String, Object> requestToApi(Map<String, Object> requestBody) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + aiApiKey);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
    return restTemplate.postForObject(aiApiUrl, request, Map.class);
  }

  @SuppressWarnings("unchecked")
  private String extractMessageContent(Map<String, Object> response) {
    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
    Map<String, Object> choice = choices.get(0);
    Map<String, String> message = (Map<String, String>) choice.get("message");
    return message.get("content");
  }

  private String extractJsonContent(String content) {
    if (content.contains("```json")) {
      content = content.substring(content.indexOf("```json") + 7);
      content = content.substring(0, content.indexOf("```"));
    } else if (content.contains("```")) {
      content = content.substring(content.indexOf("```") + 3);
      content = content.substring(0, content.indexOf("```"));
    }
    return content.trim();
  }

  private String processFloatingPoints(String content) {
    Pattern p = Pattern.compile("(\\d+)\\.(?!\\d)");
    Matcher m = p.matcher(content);
    StringBuilder sb = new StringBuilder();
    while (m.find()) {
      m.appendReplacement(sb, m.group(1) + ".0");
    }
    m.appendTail(sb);
    return sb.toString();
  }

  public PageBlockAnalysisResult processPageContent(String rawContent, Grade grade) {
    log.info("페이지 콘텐츠 처리 시작, 난이도: {}", grade);
    try {
      String systemPrompt = new PromptBuilder()
          .add(PromptBuilder.BLOCK_SYSTEM_PROMPT, Map.of("grade", grade.name()))
          .build();

      String userPrompt = "다음 교육 자료를 Block 구조(JSON)로 변환해 주세요: \n\n" + rawContent;

      Map<String, Object> requestBody = new ChatRequestBuilder()
          .model(MODEL)
          .temperature(0.3)
          .systemMessage(systemPrompt)
          .userMessage(userPrompt)
          .build();

      Map<String, Object> response = requestToApi(requestBody);
      log.info("AI 블록생성 응답: {}", response);

      String content = extractMessageContent(response);
      content = extractJsonContent(content);
      
      // JSON 문자열 전처리
      content = content.replaceAll("\\r\\n|\\r|\\n", " ") // 줄바꿈 문자를 공백으로 변환
                      .replaceAll("\\s+", " ") // 연속된 공백을 하나로 통합
                      .trim(); // 앞뒤 공백 제거
      
      if (!(content.startsWith("[") && content.endsWith("]"))) {
        log.info("AI 응답이 Block 구조(JSON 배열)가 아님. content: {}", content);
        return new PageBlockAnalysisResult(content, new ArrayList<>());
      }

      content = processFloatingPoints(content);

      try {
        List<BlockImpl> blockImpls = objectMapper.readValue(content, new TypeReference<>() {});
        List<Block> blocks = new ArrayList<>(blockImpls);
        blockImpls.forEach(block -> {
          log.info(block.toString());
        });

        log.info("페이지 콘텐츠 처리 완료");
        return new PageBlockAnalysisResult(content, blocks);
      } catch (Exception e) {
        log.error("Block 구조 JSON 파싱 실패. 원본: {}", content, e);
        throw new RuntimeException("AI 응답 JSON 파싱 실패: " + e.getMessage(), e);
      }
    } catch (Exception e) {
      log.error("페이지 콘텐츠 처리 중 오류 발생", e);
      throw new RuntimeException("AI를 통한 페이지 처리 중 오류가 발생했습니다.", e);
    }
  }

  public String extractSectionTitle(String rawContent) {
    log.info("섹션 제목 추출 시작");

    try {
      String userPrompt =
          """
          다음 텍스트에서 섹션의 제목을 추출하거나 제목을 생성하려고 합니다.
          제목을 생성하는 기준: 제목이 명시적으로 존재하지 않거나 여러 주제를 포함함
          제목일 가능성이 높은 기준: 문서의 가장 앞에 위치하며, 다른 문장에 비해 짧고 간결함
          생성 시 결과: 전체 내용을 요약한 핵심 주제 기반
          최종 결과: 20자 이내의 내용을 포괄적으로 이해할 수 있는 문맥상 자연스러운 제목
          
          """
          + (rawContent.length() > 1000 ? rawContent.substring(0, 1000) : rawContent);

      Map<String, Object> requestBody = new ChatRequestBuilder()
          .model(MODEL)
          .temperature(0.3)
          .systemMessage(PromptBuilder.SECTION_TITLE_SYSTEM_PROMPT)
          .userMessage(userPrompt)
          .build();

      Map<String, Object> response = requestToApi(requestBody);
      String title = extractMessageContent(response).trim();
      title = title.replaceAll("^\"|\"$|^'|'$|\\.$", "");

      log.info("섹션 제목 추출 완료: {}", title);
      return title;

    } catch (Exception e) {
      log.error("섹션 제목 추출 중 오류 발생", e);
      return "제목 없음";
    }
  }

  public Integer calculateReadingLevel(String content) {
    log.info("읽기 난이도 계산 시작");

    try {
      String userPrompt = "다음 텍스트의 읽기 난이도를 분석하여 1(매우 쉬움)부터 10(매우 어려움)까지의 숫자로만 응답하세요.: \n\n" +
                          (content.length() > 1000 ? content.substring(0, 1000) : content);

      Map<String, Object> requestBody = new ChatRequestBuilder()
          .model(MODEL)
          .temperature(0.3)
          .systemMessage(PromptBuilder.READING_LEVEL_SYSTEM_PROMPT)
          .userMessage(userPrompt)
          .build();

      Map<String, Object> response = requestToApi(requestBody);
      String levelStr = extractMessageContent(response).trim();
      levelStr = levelStr.replaceAll("[^0-9]", "");
      int level = Integer.parseInt(levelStr);

      level = Math.max(1, Math.min(10, level));

      log.info("읽기 난이도 계산 완료: {}", level);
      return level;

    } catch (Exception e) {
      log.error("읽기 난이도 계산 중 오류 발생", e);
      return 5; // 오류 시 중간 값으로 대체
    }
  }

  public Integer countWords(String content) {
    if (content == null || content.isEmpty()) {
      return 0;
    }
    String[] words = content.trim().split("\\s+");
    return words.length;
  }

  public Float calculateComplexityScore(String content) {
    try {
      return calculateReadingLevel(content) / 10.0f;
    } catch (Exception e) {
      log.error("복잡도 점수 계산 중 오류 발생", e);
      return 0.5f;
    }
  }

  public List<TermInfo> extractTerms(String content, Grade grade) {
    log.info("키워드 추출 시작");
    try {
      String systemPrompt = new PromptBuilder()
          .add(PromptBuilder.TERM_EXTRACT_SYSTEM_PROMPT, Map.of("grade", grade.name()))
          .build();

      String userPrompt = "다음 교육 자료에서 어려운 용어를 찾고 설명해 주세요: \n\n" + content;

      Map<String, Object> requestBody = new ChatRequestBuilder()
          .model(MODEL)
          .temperature(0.3)
          .systemMessage(systemPrompt)
          .userMessage(userPrompt)
          .build();

      Map<String, Object> response = requestToApi(requestBody);
      String content2 = extractMessageContent(response).trim();
      content2 = extractJsonContent(content2);

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> termsData = objectMapper.readValue(content2, List.class);
      List<TermInfo> terms = new ArrayList<>();

      for (Map<String, Object> termData : termsData) {
        String term = (String) termData.get("term");
        String explanation = (String) termData.get("explanation");

        @SuppressWarnings("unchecked")
        Map<String, Integer> position = (Map<String, Integer>) termData.get("position");
        int start = position.get("start");
        int end = position.get("end");

        String typeStr = (String) termData.get("type");
        TermType type = TermType.valueOf(typeStr);

        Boolean visualAidNeeded = (Boolean) termData.get("visualAidNeeded");
        String readAloudText = (String) termData.get("readAloudText");

        Map<String, Integer> positionMap = new HashMap<>();
        positionMap.put("start", start);
        positionMap.put("end", end);
        com.fasterxml.jackson.databind.JsonNode positionJson = objectMapper.valueToTree(positionMap);

        terms.add(new TermInfo(term, explanation, positionJson, type, visualAidNeeded, readAloudText));
      }

      log.info("키워드 추출 완료: {} 개 용어", terms.size());
      return terms;
    } catch (Exception e) {
      log.error("키워드 추출 중 오류 발생", e);
      return new ArrayList<>();
    }
  }

  public List<ImageInfo> extractOrGenerateImages(String content, List<TermInfo> terms) {
        log.info("이미지 생성 시작");
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4");

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "당신은 교육 자료에서 시각적 지원이 필요한 개념을 식별하고, " +
                "설명하는 이미지를 생성하는 전문가입니다. 생성하는 이미지 설명에서는 다음 규칙을 반드시 지켜주세요:\n" +
                "1. 고유명사나 캐릭터 이름은 일반적인 용어로 대체하세요 (예: '앨리스' → '소녀', '에스콰이어' → '호칭')\n" +
                "2. 문맥을 모르면 이해하기 어려운 용어는 설명을 추가하세요\n" +
                "3. 초등학생이 이해할 수 있는 보편적인 개념과 표현만 사용하세요\n\n" +
                "반드시 아래 JSON 배열 형식으로만 응답해 주세요:\n" +
                "[{\"description\": \"생성할 이미지의 설명\", \"imageType\": \"CONCEPT_VISUALIZATION | DIAGRAM | COMPARISON_CHART | EXAMPLE_ILLUSTRATION\", " +
                "\"conceptReference\": \"관련 개념\", \"altText\": \"이미지 대체 텍스트\", \"position\": {\"page\": 페이지번호}}]");
            messages.add(systemMessage);

            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("다음 교육 자료와 어려운 용어 목록을 분석하여, 필요한 이미지를 생성해 주세요:\n\n");
            promptBuilder.append("교육 자료:\n").append(content).append("\n\n");

            promptBuilder.append("어려운 용어 목록:\n");
            for (TermInfo term : terms) {
                if (term.isVisualAidNeeded()) {
                    promptBuilder.append("- ").append(term.getTerm()).append(": ").append(term.getExplanation()).append("\n");
                }
            }

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", promptBuilder.toString());
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.5);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + aiApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            Map<String, Object> response = restTemplate.postForObject(aiApiUrl, request, Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> choice = choices.get(0);
            Map<String, String> message = (Map<String, String>) choice.get("message");
            String content2 = message.get("content");

            log.info("AI 이미지 원본 응답: {}", content2);

            if (content2.contains("```json")) {
                content2 = content2.substring(content2.indexOf("```json") + 7);
                content2 = content2.substring(0, content2.indexOf("```"));
            } else if (content2.contains("```")) {
                content2 = content2.substring(content2.indexOf("```") + 3);
                content2 = content2.substring(0, content2.indexOf("```"));
            }
            content2 = content2.trim();

            // 소수점 뒤에 숫자가 없는 경우(예: 1.)를 1.0으로 보정
            Pattern p = Pattern.compile("(\\d+)\\.(?!\\d)");
            Matcher m = p.matcher(content2);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, m.group(1) + ".0");
            }
            m.appendTail(sb);
            content2 = sb.toString();

            try {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> imagesData = objectMapper.readValue(content2, List.class);
                List<ImageInfo> images = new ArrayList<>();
                
                // 각 이미지 설명에 대해 Replicate API로 이미지 생성
                for (Map<String, Object> imageData : imagesData) {
                    // 이미지 생성을 위한 프롬프트 구성
                    String description = (String) imageData.get("description");
                    String imageTypeStr = (String) imageData.get("imageType");
                    ImageType imageType = ImageType.valueOf(imageTypeStr);
                    String conceptReference = (String) imageData.get("conceptReference");
                    String altText = (String) imageData.get("altText");
                    
                    String imageUrl = generateImageWithReplicate(description);
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> position = (Map<String, Object>) imageData.get("position");
                    com.fasterxml.jackson.databind.JsonNode positionJson = objectMapper.valueToTree(position);
                    
                    String localFilePath = saveImageToLocalFile(imageUrl, conceptReference);
                    if (!localFilePath.isEmpty()) {
                        imageUrl = localFilePath; // imageUrl 변수를 로컬 경로로 업데이트
                    }
                    
                    images.add(new ImageInfo(imageUrl, imageType, conceptReference, altText, positionJson));
                }
                
                log.info("이미지 생성 완료: {} 개 이미지", images.size());
                return images;
            } catch (Exception e) {
                log.error("이미지 JSON 파싱 실패. 원본: {}", content2, e);
                throw new RuntimeException("AI 이미지 응답 JSON 파싱 실패: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("이미지 생성 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

  private String generateImageWithReplicate(String description) {
    try {
      String prompt = "교육용 이미지: " + description +
          "\n\n지시사항:" +
          "\n- 복잡한 배경이나 불필요한 요소는 제거해주세요" +
                "\n- 텍스트는 한글을 사용해주세요";

      String selectedStyle = "realistic_image";
      
      Map<String, Object> input = new HashMap<>();
      input.put("prompt", prompt);
      input.put("style", selectedStyle);
      input.put("size", "1024x1024");
      
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("version", "recraft-ai/recraft-v3");
      requestBody.put("input", input);
      
      String jsonBody = objectMapper.writeValueAsString(requestBody);
      log.info("Replicate API 요청: {}", jsonBody);
      
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(replicateConfig.getUrl()))
          .header("Content-Type", "application/json")
          .header("Authorization", "Token " + replicateConfig.getKey())
          .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
          .build();
      
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      String responseBody = response.body();
      log.info("Replicate API 응답 (status: {}): {}", response.statusCode(), responseBody);
      
      if (response.statusCode() != 201) {
        log.error("Replicate API 호출 실패. 상태 코드: {}, 응답: {}", response.statusCode(), responseBody);
        return "";
      }
      
      JsonNode responseJson = objectMapper.readTree(responseBody);
      
      if (!responseJson.has("id")) {
        log.error("Replicate API 응답에 id 필드가 없습니다: {}", responseBody);
        return "";
      }
      
      String predictionId = responseJson.get("id").asText();
      log.info("Prediction ID: {}", predictionId);
      
      String getUrl = replicateConfig.getUrl() + "/" + predictionId;
      int maxRetries = 20;
      int retryCount = 0;
      
      while (retryCount < maxRetries) {
        Thread.sleep(3000);
        
        HttpRequest getRequest = HttpRequest.newBuilder()
            .uri(URI.create(getUrl))
            .header("Authorization", "Token " + replicateConfig.getKey())
            .GET()
            .build();
        
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        String getResponseBody = getResponse.body();
        
        log.info("Replicate API 폴링 응답 #{} (status: {})", retryCount + 1, getResponse.statusCode());
        
        JsonNode getResponseJson = objectMapper.readTree(getResponseBody);
        
        if (getResponseJson.has("status")) {
          String status = getResponseJson.get("status").asText();
          
          if ("succeeded".equals(status)) {
            if (getResponseJson.has("output")) {
              JsonNode output = getResponseJson.get("output");
              
              String imageUrl = null;
              if (output != null && !output.isNull()) {
                if (output.isArray() && output.size() > 0) {
                  imageUrl = output.get(0).asText();
                } else if (output.isTextual()) {
                  imageUrl = output.asText();
                }
                
                if (imageUrl != null && !imageUrl.isEmpty()) {
                  log.info("이미지 URL 생성 성공: {}", imageUrl);
                  return imageUrl;
                }
              }
              
              log.warn("Output 필드가 비어있거나 예상된 형식이 아닙니다. Raw output: {}", output);
              return "";
            } else {
              log.warn("Status가 succeeded이지만 output 필드가 없습니다: {}", getResponseBody);
              return "";
            }
          } else if ("failed".equals(status)) {
            String error = getResponseJson.has("error") ? getResponseJson.get("error").asText() : "알 수 없는 오류";
            log.error("이미지 생성 실패: {}", error);
            return "";
          } else if ("canceled".equals(status)) {
            log.error("이미지 생성이 취소되었습니다.");
            return "";
          } else {
            log.info("이미지 생성 상태: {}, 다시 시도합니다...", status);
          }
        }
        
        retryCount++;
      }
      
      log.error("최대 재시도 횟수({})에 도달했습니다. 이미지 URL을 가져오지 못했습니다.", maxRetries);
      return "";
      
    } catch (Exception e) {
      log.error("Replicate API를 사용한 이미지 생성 중 오류 발생: {}", e.getMessage(), e);
      return "";
    }
  }

  private String saveImageToLocalFile(String imageUrl, String conceptReference) {
    try {
      String pdfFolderPath = DocumentProcessHolder.getPdfFolderPath();
      
      if (pdfFolderPath == null || pdfFolderPath.isEmpty()) {
        log.warn("PDF 폴더 경로가 없습니다. 이미지를 로컬에 저장하지 않고 URL만 반환합니다.");
        return imageUrl;
      }
      
      Path directory = Paths.get(pdfFolderPath);
      if (!Files.exists(directory)) {
        Files.createDirectories(directory);
        log.info("PDF 폴더 생성 완료: {}", directory.toAbsolutePath());
      }
      
      String sanitizedName = conceptReference.replaceAll("[^a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\s]", "_")
                                          .replaceAll("\\s+", "_");
      String fileName = sanitizedName + "_" + System.currentTimeMillis() + ".png";
      Path filePath = directory.resolve(fileName);
      
      URL url = new URL(imageUrl);
      try (java.io.InputStream in = url.openStream()) {
        Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
      }
      
      log.info("이미지가 PDF와 동일한 폴더에 저장되었습니다: {}", filePath);
      
      String relativePath = pdfFolderPath;
      if (relativePath.startsWith(uploadDir)) {
        relativePath = relativePath.substring(uploadDir.length());
      }
      if (relativePath.startsWith("/")) {
        relativePath = relativePath.substring(1);
      }
      
      return "/" + relativePath + "/" + fileName;
      
    } catch (Exception e) {
      log.error("이미지를 로컬에 저장하는 중 오류 발생: {}", e.getMessage(), e);
      return imageUrl;
    }
  }

  public String translateTextWithOpenAI(String text) {
    log.info("OpenAI 번역 시작: 텍스트 길이: {}", text.length());
    try {
      String systemPrompt = "영어 텍스트를 한국어로 자연스럽게 번역하세요. 번역 결과만 반환하세요. 설명, 마크다운, 코드블록 없이 번역문만 출력하세요.";

      Map<String, Object> requestBody = new ChatRequestBuilder()
          .model(MODEL)
          .temperature(0.3)
          .systemMessage(systemPrompt)
          .userMessage(text)
          .build();

      Map<String, Object> response = requestToApi(requestBody);
      String content = extractMessageContent(response);

      if (content == null) {
        return "";
      }
      
      content = content.trim();
      if (content.startsWith("\"")) {
        content = content.substring(1);
      }
      if (content.endsWith("\"")) {
        content = content.substring(0, content.length() - 1);
      }
      return content;
    } catch (Exception e) {
      log.error("OpenAI 번역 실패: {}", e.getMessage());
      throw new RuntimeException("OpenAI 번역 실패: " + e.getMessage(), e);
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TermInfo {
    private String term;
    private String explanation;
    private com.fasterxml.jackson.databind.JsonNode positionJson;
    private TermType termType;
    private boolean visualAidNeeded;
    private String readAloudText;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ImageInfo {
    private String imageUrl;
    private ImageType imageType;
    private String conceptReference;
    private String altText;
    private com.fasterxml.jackson.databind.JsonNode positionJson;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PageBlockAnalysisResult {
    private String originalContent;
    private List<Block> blocks;
  }
}
