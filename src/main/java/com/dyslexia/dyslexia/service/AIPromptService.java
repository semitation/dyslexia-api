package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.domain.pdf.Block;
import com.dyslexia.dyslexia.domain.pdf.BlockImpl;
import com.dyslexia.dyslexia.enums.Grade;
import com.dyslexia.dyslexia.enums.ImageType;
import com.dyslexia.dyslexia.enums.TermType;
import com.dyslexia.dyslexia.util.ChatRequestBuilder;
import com.dyslexia.dyslexia.util.PromptBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

  public String processPageContent(String rawContent, Grade grade) {
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

      if (content.contains("```json")) {
        content = content.substring(content.indexOf("```json") + 7);
        content = content.substring(0, content.indexOf("```"));
      } else if (content.contains("```")) {
        content = content.substring(content.indexOf("```") + 3);
        content = content.substring(0, content.indexOf("```"));
      }

      content = content.trim();
      objectMapper.readValue(content, List.class);
      log.info("페이지 콘텐츠 처리 완료");
      return content;
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
          최종 결과: 20자 이내의 간결하면서 핵심적이지만 난독증 환자도 읽기 쉬운 문맥상 자연스러운 제목
          
          
          """
          +
          // "다음 텍스트에서 섹션 제목을 추출하세요. 명확한 제목이 없으면 내용을 포괄적으로 이해할 수 있도록 단 하나의 제목을 생성하세요. 한 문장으로만 출력하세요.: \n\n"
          // +
          (rawContent.length() > 1000 ? rawContent.substring(0, 1000) : rawContent);

      Map<String, Object> requestBody = new ChatRequestBuilder()
          .model(MODEL)
          .temperature(0.3)
          .systemMessage(PromptBuilder.SECTION_TITLE_SYSTEM_PROMPT)
          .userMessage(userPrompt)
          .build();

      Map<String, Object> response = requestToApi(requestBody);
      log.info("AI 블록생성 응답: {}", response);

      String title = extractMessageContent(response).trim();

      // 불필요한 따옴표, 마침표 등 제거
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

      if (level < 1) {
        level = 1;
      }
      if (level > 10) {
        level = 10;
      }

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

  // 텍스트의 복잡도 점수 계산 (0.0-1.0 사이 값)
  public Float calculateComplexityScore(String content) {
    try {
      // 읽기 난이도를 0.0-1.0 사이 값으로 변환
      return calculateReadingLevel(content) / 10.0f;
    } catch (Exception e) {
      log.error("복잡도 점수 계산 중 오류 발생", e);
      return 0.5f;
    }
  }

  // 키워드/어려운 용어를 추출하고 설명을 생성
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

      if (content2.contains("```json")) {
        content2 = content2.substring(content2.indexOf("```json") + 7);
        content2 = content2.substring(0, content2.indexOf("```"));
      } else if (content2.contains("```")) {
        content2 = content2.substring(content2.indexOf("```") + 3);
        content2 = content2.substring(0, content2.indexOf("```"));
      }

      content2 = content2.trim();

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
        com.fasterxml.jackson.databind.JsonNode positionJson = objectMapper.valueToTree(
            positionMap);

        terms.add(
            new TermInfo(term, explanation, positionJson, type, visualAidNeeded, readAloudText));
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

      StringBuilder promptBuilder = new StringBuilder();
      promptBuilder.append("다음 교육 자료와 어려운 용어 목록을 분석하여, 필요한 이미지를 생성해 주세요:\n\n");
      promptBuilder.append("교육 자료:\n").append(content).append("\n\n");

      promptBuilder.append("어려운 용어 목록:\n");
      for (TermInfo term : terms) {
        if (term.isVisualAidNeeded()) {
          promptBuilder.append("- ").append(term.getTerm()).append(": ")
              .append(term.getExplanation()).append("\n");
        }
      }

      Map<String, Object> requestBody = new ChatRequestBuilder()
          .model(MODEL)
          .temperature(0.5)
          .systemMessage(PromptBuilder.IMAGE_EXTRACT_SYSTEM_PROMPT)
          .userMessage(promptBuilder.toString())
          .build();

      Map<String, Object> response = requestToApi(requestBody);

      String content2 = extractMessageContent(response).trim();

      log.info("AI 이미지 원본 응답: {}", content2);

      if (content2.contains("```json")) {
        content2 = content2.substring(content2.indexOf("```json") + 7);
        content2 = content2.substring(0, content2.indexOf("```"));
      } else if (content2.contains("```")) {
        content2 = content2.substring(content2.indexOf("```") + 3);
        content2 = content2.substring(0, content2.indexOf("```"));
      }
      content2 = content2.trim();

      Pattern p = Pattern.compile("(\\d+)\\.(?!\\d)");
      Matcher m = p.matcher(content2);
      StringBuilder sb = new StringBuilder();
      while (m.find()) {
        m.appendReplacement(sb, m.group(1) + ".0");
      }
      m.appendTail(sb);
      content2 = sb.toString();

      try {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> imagesData = objectMapper.readValue(content2, List.class);
        List<ImageInfo> images = new ArrayList<>();
        for (Map<String, Object> imageData : imagesData) {
          String imageUrl = (String) imageData.get("imageUrl");
          String imageTypeStr = (String) imageData.get("imageType");
          ImageType imageType = ImageType.valueOf(imageTypeStr);
          String conceptReference = (String) imageData.get("conceptReference");
          String altText = (String) imageData.get("altText");
          @SuppressWarnings("unchecked")
          Map<String, Object> position = (Map<String, Object>) imageData.get("position");
          com.fasterxml.jackson.databind.JsonNode positionJson = objectMapper.valueToTree(position);
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

  public List<Block> processPageContentToBlocks(String rawContent, Grade grade) {
    log.info("페이지 콘텐츠 Block 구조로 처리 시작, 난이도: {}", grade);
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

      String content = extractMessageContent(response);

      log.info("AI 원본 응답: {}", content);

      if (content.contains("```json")) {
        content = content.substring(content.indexOf("```json") + 7);
        content = content.substring(0, content.indexOf("```"));
      } else if (content.contains("```")) {
        content = content.substring(content.indexOf("```") + 3);
        content = content.substring(0, content.indexOf("```"));
      }
      content = content.trim();

      if (!(content.startsWith("[") && content.endsWith("]"))) {
        log.info("AI 응답이 Block 구조(JSON 배열)가 아님. content: {}", content);
        return new ArrayList<>();
      }

      Pattern p = Pattern.compile("(\\d+)\\.(?!\\d)");
      Matcher m = p.matcher(content);
      StringBuilder sb = new StringBuilder();
      while (m.find()) {
        m.appendReplacement(sb, m.group(1) + ".0");
      }
      m.appendTail(sb);
      content = sb.toString();

      try {
        List<BlockImpl> blockImpls = objectMapper.readValue(content,
            new TypeReference<>() {
            });
        List<Block> blocks = new ArrayList<>(blockImpls);
        log.info("Block 구조 변환 완료: {}개 블록", blocks.size());
        return blocks;
      } catch (Exception e) {
        log.error("Block 구조 JSON 파싱 실패. 원본: {}", content, e);
        throw new RuntimeException("AI 응답 JSON 파싱 실패: " + e.getMessage(), e);
      }
    } catch (Exception e) {
      log.error("Block 구조 변환 중 오류 발생", e);
      throw new RuntimeException("AI를 통한 Block 구조 변환 중 오류가 발생했습니다.", e);
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

      log.info("OpenAI 번역 응답: {}", response);

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
} 