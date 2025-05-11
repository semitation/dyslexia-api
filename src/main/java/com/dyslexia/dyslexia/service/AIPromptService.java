package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.enums.Grade;
import com.dyslexia.dyslexia.enums.ImageType;
import com.dyslexia.dyslexia.enums.TermType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIPromptService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${ai.api.url}")
    private String aiApiUrl;
    
    @Value("${ai.api.key}")
    private String aiApiKey;

    public String processPageContent(String rawContent, Grade grade) {
        log.info("페이지 콘텐츠 처리 시작, 난이도: {}", grade);
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4");
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "당신은 난독증이 있는 " + grade.name() + " 학생들을 위한 교육 자료를 변환하는 전문가입니다. " +
                    "텍스트를 분석하고 구조화된 JSON 형식으로 변환해 주세요. 각 섹션, 단락, 어려운 용어를 식별하고 " +
                    "학생들이 더 쉽게 이해할 수 있도록 단순화된 형태로 조직화해 주세요.");
            messages.add(systemMessage);
            
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", "다음 교육 자료를 난독증 학생들을 위한 구조화된 JSON으로 변환해 주세요: \n\n" + rawContent);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + aiApiKey);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            Map<String, Object> response = restTemplate.postForObject(aiApiUrl, request, Map.class);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> choice = choices.get(0);
            Map<String, String> message = (Map<String, String>) choice.get("message");
            String content = message.get("content");
            
            if (content.contains("```json")) {
                content = content.substring(content.indexOf("```json") + 7);
                content = content.substring(0, content.indexOf("```"));
            } else if (content.contains("```")) {
                content = content.substring(content.indexOf("```") + 3);
                content = content.substring(0, content.indexOf("```"));
            }
            
            content = content.trim();
            
            objectMapper.readValue(content, Map.class);
            
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
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4");
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "당신은 교육 자료에서 섹션 제목을 추출하는 전문가입니다.");
            messages.add(systemMessage);
            
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", "다음 텍스트에서 섹션 제목을 추출해 주세요. 명확한 제목이 없으면 내용을 가장 잘 대표하는 제목을 생성해 주세요: \n\n" + 
                    (rawContent.length() > 1000 ? rawContent.substring(0, 1000) : rawContent));
            messages.add(userMessage);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + aiApiKey);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            Map<String, Object> response = restTemplate.postForObject(aiApiUrl, request, Map.class);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> choice = choices.get(0);
            Map<String, String> message = (Map<String, String>) choice.get("message");
            String title = message.get("content").trim();
            
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
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4");
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "당신은 텍스트의 읽기 난이도를 계산하는 전문가입니다. 1부터 10까지의 숫자로만 응답해 주세요.");
            messages.add(systemMessage);
            
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", "다음 텍스트의 읽기 난이도를 분석하여 1(매우 쉬움)부터 10(매우 어려움)까지의 숫자로만 응답해 주세요: \n\n" + 
                    (content.length() > 1000 ? content.substring(0, 1000) : content));
            messages.add(userMessage);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + aiApiKey);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            Map<String, Object> response = restTemplate.postForObject(aiApiUrl, request, Map.class);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> choice = choices.get(0);
            Map<String, String> message = (Map<String, String>) choice.get("message");
            String levelStr = message.get("content").trim();
            
            levelStr = levelStr.replaceAll("[^0-9]", "");
            int level = Integer.parseInt(levelStr);
            
            if (level < 1) level = 1;
            if (level > 10) level = 10;
            
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
            return  calculateReadingLevel(content) / 10.0f;
        } catch (Exception e) {
            log.error("복잡도 점수 계산 중 오류 발생", e);
            return 0.5f;
        }
    }
    
     // 키워드/어려운 용어를 추출하고 설명을 생성
    public List<TermInfo> extractTerms(String content, Grade grade) {
        log.info("키워드 추출 시작");
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4");
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "당신은 교육 자료에서 난독증이 있는 " + grade.name() + " 학생들이 이해하기 어려울 수 있는 용어를 찾고 " +
                    "쉽게 설명하는 전문가입니다. 전문 용어, 복잡한 개념, 추상적인 아이디어 등을 찾아 간단히 설명해 주세요. " +
                    "각 용어는 다음 JSON 형식으로 응답해 주세요:\n" +
                    "[{\"term\": \"용어\", \"explanation\": \"쉬운 설명\", \"position\": {\"start\": 시작위치, \"end\": 끝위치}, " +
                    "\"type\": \"DIFFICULT_WORD | COMPLEX_CONCEPT | ABSTRACT_IDEA | TECHNICAL_TERM\", " +
                    "\"visualAidNeeded\": true|false, \"readAloudText\": \"소리내어 읽기 텍스트\"}]");
            messages.add(systemMessage);
            
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", "다음 교육 자료에서 어려운 용어를 찾고 설명해 주세요: \n\n" + content);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3);
            
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
            
            // content에서 JSON 부분만 추출
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
                    "설명하는 이미지를 생성하는 전문가입니다. 다음 JSON 형식으로 응답해 주세요:\n" +
                    "[{\"imageUrl\": \"생성할 이미지의 설명\", \"imageType\": \"CONCEPT_VISUALIZATION | DIAGRAM | COMPARISON_CHART | EXAMPLE_ILLUSTRATION\", " +
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
            
            if (content2.contains("```json")) {
                content2 = content2.substring(content2.indexOf("```json") + 7);
                content2 = content2.substring(0, content2.indexOf("```"));
            } else if (content2.contains("```")) {
                content2 = content2.substring(content2.indexOf("```") + 3);
                content2 = content2.substring(0, content2.indexOf("```"));
            }
            
            content2 = content2.trim();
            
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
                
                // TODO: 실제 이미지 생성 로직은 여기서 구현해야 함 - 은기
                images.add(new ImageInfo(imageUrl, imageType, conceptReference, altText, positionJson));
            }
            
            log.info("이미지 생성 완료: {} 개 이미지", images.size());
            return images;
            
        } catch (Exception e) {
            log.error("이미지 생성 중 오류 발생", e);
            return new ArrayList<>();
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