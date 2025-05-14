package com.dyslexia.dyslexia.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class VocabularyAnalysisPromptService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.api.url}")
    private String aiApiUrl;
    @Value("${ai.api.key}")
    private String aiApiKey;

    private static final String MODEL = "gpt-4o-mini";

    public String analyzeVocabulary(String text, int targetGradeLevel) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", getSystemPrompt());
            messages.add(systemMessage);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", getUserPrompt(text, targetGradeLevel));
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + aiApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            Map<String, Object> response = restTemplate.postForObject(aiApiUrl, request, Map.class);
            log.info("어휘 분석 AI 요청: {}", objectMapper.writeValueAsString(requestBody));
            log.info("어휘 분석 AI 응답: {}", response);

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
            log.info("어휘 분석 결과 JSON: {}", content);
            return content;
        } catch (Exception e) {
            log.error("어휘 분석 프롬프트/AI 호출 실패", e);
            return "[]";
        }
    }

    public String analyzeVocabularyBasic(String text, int targetGradeLevel) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", getBasicSystemPrompt());
            messages.add(systemMessage);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", getBasicUserPrompt(text, targetGradeLevel));
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + aiApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            Map<String, Object> response = restTemplate.postForObject(aiApiUrl, request, Map.class);
            log.info("어휘 분석(BASIC) AI 요청: {}", objectMapper.writeValueAsString(requestBody));
            log.info("어휘 분석(BASIC) AI 응답: {}", response);

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
            log.debug("어휘 분석(BASIC) 결과 JSON: {}", content);
            return content;
        } catch (Exception e) {
            log.error("어휘 분석(BASIC) 프롬프트/AI 호출 실패", e);
            return "[]";
        }
    }

    public String analyzePhonemeAnalysis(String word) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", getPhonemeSystemPrompt());
            messages.add(systemMessage);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", getPhonemeUserPrompt(word));
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + aiApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            Map<String, Object> response = restTemplate.postForObject(aiApiUrl, request, Map.class);
            log.debug("음소분해/학습팁 AI 요청: {}", objectMapper.writeValueAsString(requestBody));
            log.info("음소분해/학습팁 AI 응답: {}", response);

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
            log.debug("음소분해/학습팁 결과 JSON: {}", content);
            return content;
        } catch (Exception e) {
            log.error("음소분해/학습팁 프롬프트/AI 호출 실패", e);
            return "{}";
        }
    }

    private String getSystemPrompt() {
        return "당신은 9-13세 난독증이 있는 초등학생을 위한 어휘 분석 전문가입니다.\n" +
                "주어진 문장에서 해당 연령대의 학생들이 읽고 이해하기 어려운 단어를 찾아서 JSON 형식으로 분석 결과를 제공해주세요.\n" +
                "어려운 어휘 판단 기준:\n" +
                "1. 3음절 이상의 복합어\n" +
                "2. 초등 3-4학년 수준을 넘는 어휘\n" +
                "3. 한자어 및 외래어 (단, 일상적으로 쓰이는 것 제외)\n" +
                "4. 추상적 개념어\n" +
                "5. 전문용어 및 학술용어\n" +
                "주의사항:\n" +
                "- 조사, 어미, 접속사는 제외\n" +
                "- 일상생활에서 자주 쓰이는 단어는 3음절 이상이라도 제외\n" +
                "- 학생의 연령과 인지 수준을 고려";
    }

    private String getUserPrompt(String text, int targetGradeLevel) {
        return String.format(
            "다음 문장에서 9-13세 난독증 학생이 어려워할 수 있는 어휘를 추출하고, 각 어휘를 음소 단위로 분해하고, 학습에 도움이 되는 팁까지 포함하여 아래 JSON 배열 형식으로만 응답하세요(코드블록 없이, 반드시 배열로):\n" +
            "\n문장: \"%s\"\n" +
            "\n예시:\n" +
            "[\n" +
            "  {\n" +
            "    \"word\": \"감각\",\n" +
            "    \"syllables\": [\n" +
            "      {\n" +
            "        \"syllable\": \"감\",\n" +
            "        \"order\": 1,\n" +
            "        \"components\": {\n" +
            "          \"initial\": {\"consonant\": \"ㄱ\", \"pronunciation\": \"기역\", \"sound\": \"/g/\", \"writingOrder\": 1, \"strokes\": 2, \"difficulty\": \"easy\"},\n" +
            "          \"medial\": {\"vowel\": \"ㅏ\", \"pronunciation\": \"아\", \"sound\": \"/a/\", \"writingOrder\": 2, \"strokes\": 2, \"difficulty\": \"easy\"},\n" +
            "          \"final\": {\"consonant\": \"ㅁ\", \"pronunciation\": \"미음\", \"sound\": \"/m/\", \"writingOrder\": 3, \"strokes\": 4, \"difficulty\": \"medium\"}\n" +
            "        },\n" +
            "        \"combinedSound\": \"/gam/\",\n" +
            "        \"writingTips\": \"ㄱ을 먼저 쓰고, ㅏ를 그 옆에, 마지막에 ㅁ을 아래에 써주세요\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"totalPhonemes\": {\n" +
            "      \"consonants\": [\"ㄱ\", \"ㅁ\", \"ㄱ\"],\n" +
            "      \"vowels\": [\"ㅏ\", \"ㅏ\"],\n" +
            "      \"uniquePhonemes\": [\"ㄱ\", \"ㅏ\", \"ㅁ\"]\n" +
            "    },\n" +
            "    \"difficultyLevel\": \"medium\",\n" +
            "    \"writingOrder\": [\n" +
            "      { \"step\": 1, \"phoneme\": \"ㄱ\", \"syllable\": \"감\" },\n" +
            "      { \"step\": 2, \"phoneme\": \"ㅏ\", \"syllable\": \"감\" },\n" +
            "      { \"step\": 3, \"phoneme\": \"ㅁ\", \"syllable\": \"감\" }\n" +
            "    ],\n" +
            "    \"learningTips\": {\n" +
            "      \"commonMistakes\": [\"ㅁ 받침을 빼먹기 쉬워요\"],\n" +
            "      \"practiceWords\": [\"강각\", \"감정\", \"각도\"],\n" +
            "      \"rhymingWords\": [\"암각\", \"담각\"]\n" +
            "    }\n" +
            "  }\n" +
            "]\n"
        , text);
    }

    private String getBasicSystemPrompt() {
        return "당신은 9-13세 난독증이 있는 초등학생을 위한 어휘 분석 전문가입니다.\n" +
                "주어진 문장에서 해당 연령대의 학생들이 읽고 이해하기 어려운 단어를 찾아서 아래 JSON 배열 형식으로 분석 결과를 제공해주세요.\n" +
                "어려운 어휘 판단 기준:\n" +
                "1. 3음절 이상의 복합어\n" +
                "2. 초등 3-4학년 수준을 넘는 어휘\n" +
                "3. 한자어 및 외래어 (단, 일상적으로 쓰이는 것 제외)\n" +
                "4. 추상적 개념어\n" +
                "5. 전문용어 및 학술용어\n" +
                "주의사항:\n" +
                "- 조사, 어미, 접속사는 제외\n" +
                "- 일상생활에서 자주 쓰이는 단어는 3음절 이상이라도 제외\n" +
                "- 학생의 연령과 인지 수준을 고려";
    }

    private String getBasicUserPrompt(String text, int targetGradeLevel) {
        return String.format(
            "다음 문장에서 9-13세 난독증 학생이 어려워할 수 있는 어휘를 추출하고 분석해주세요:\n" +
            "\n문장: \"%s\"\n" +
            "\n아래 JSON 배열 형식으로만 응답하세요(코드블록 없이, 반드시 배열로):\n" +
            "[\n" +
            "  {\n" +
            "    \"word\": \"감각\",\n" +
            "    \"startIndex\": 3,\n" +
            "    \"endIndex\": 5,\n" +
            "    \"definition\": \"몸으로 느끼고 알아차리는 능력\",\n" +
            "    \"simplifiedDefinition\": \"보고, 듣고, 만지면서 알아차리는 것\",\n" +
            "    \"examples\": [\"눈으로 보는 것도 감각이에요\"],\n" +
            "    \"difficultyLevel\": \"medium\",\n" +
            "    \"reason\": \"추상적 개념어\",\n" +
            "    \"gradeLevel\": 4\n" +
            "  }\n" +
            "]\n"
        , text);
    }

    private String getPhonemeSystemPrompt() {
        return "당신은 한글 음성학 전문가입니다. 주어진 한글 단어를 음소(초성, 중성, 종성) 단위로 정확하게 분해하여 난독증 학생의 음운 학습을 돕는 데이터를 제공해주세요.";
    }

    private String getPhonemeUserPrompt(String word) {
        return String.format(
            "다음 단어를 음소별로 분해하고 학습에 필요한 정보를 제공해주세요:\n" +
            "\n단어: \"%s\"\n" +
            "\n아래 JSON 형식으로만, 코드블록 없이, 반드시 모든 필드 포함하여 응답하세요. (예시를 참고하여 동일한 구조로 작성):\n" +
            "{\n" +
            "  \"word\": \"감각\",\n" +
            "  \"syllables\": [\n" +
            "    {\n" +
            "      \"syllable\": \"감\",\n" +
            "      \"order\": 1,\n" +
            "      \"components\": {\n" +
            "        \"initial\": {\n" +
            "          \"consonant\": \"ㄱ\",\n" +
            "          \"pronunciation\": \"기역\",\n" +
            "          \"sound\": \"/g/\",\n" +
            "          \"writingOrder\": 1,\n" +
            "          \"strokes\": 2,\n" +
            "          \"difficulty\": \"easy\"\n" +
            "        },\n" +
            "        \"medial\": {\n" +
            "          \"vowel\": \"ㅏ\",\n" +
            "          \"pronunciation\": \"아\",\n" +
            "          \"sound\": \"/a/\",\n" +
            "          \"writingOrder\": 2,\n" +
            "          \"strokes\": 2,\n" +
            "          \"difficulty\": \"easy\"\n" +
            "        },\n" +
            "        \"final\": {\n" +
            "          \"consonant\": \"ㅁ\",\n" +
            "          \"pronunciation\": \"미음\",\n" +
            "          \"sound\": \"/m/\",\n" +
            "          \"writingOrder\": 3,\n" +
            "          \"strokes\": 4,\n" +
            "          \"difficulty\": \"medium\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"combinedSound\": \"/gam/\",\n" +
            "      \"writingTips\": \"ㄱ을 먼저 쓰고, ㅏ를 그 옆에, 마지막에 ㅁ을 아래에 써주세요\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"syllable\": \"각\",\n" +
            "      \"order\": 2,\n" +
            "      \"components\": {\n" +
            "        \"initial\": {\n" +
            "          \"consonant\": \"ㄱ\",\n" +
            "          \"pronunciation\": \"기역\",\n" +
            "          \"sound\": \"/g/\",\n" +
            "          \"writingOrder\": 1,\n" +
            "          \"strokes\": 2,\n" +
            "          \"difficulty\": \"easy\"\n" +
            "        },\n" +
            "        \"medial\": {\n" +
            "          \"vowel\": \"ㅏ\",\n" +
            "          \"pronunciation\": \"아\",\n" +
            "          \"sound\": \"/a/\",\n" +
            "          \"writingOrder\": 2,\n" +
            "          \"strokes\": 2,\n" +
            "          \"difficulty\": \"easy\"\n" +
            "        },\n" +
            "        \"final\": {\n" +
            "          \"consonant\": \"ㄱ\",\n" +
            "          \"pronunciation\": \"기역\",\n" +
            "          \"sound\": \"/k/\",\n" +
            "          \"writingOrder\": 3,\n" +
            "          \"strokes\": 2,\n" +
            "          \"difficulty\": \"easy\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"combinedSound\": \"/gak/\",\n" +
            "      \"writingTips\": \"ㄱ을 먼저 쓰고, ㅏ를 그 옆에, 마지막에 작은 ㄱ을 아래에 써주세요\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"totalPhonemes\": {\n" +
            "    \"consonants\": [\"ㄱ\", \"ㅁ\", \"ㄱ\"],\n" +
            "    \"vowels\": [\"ㅏ\", \"ㅏ\"],\n" +
            "    \"uniquePhonemes\": [\"ㄱ\", \"ㅏ\", \"ㅁ\"]\n" +
            "  },\n" +
            "  \"difficultyLevel\": \"medium\",\n" +
            "  \"writingOrder\": [\n" +
            "    { \"step\": 1, \"phoneme\": \"ㄱ\", \"syllable\": \"감\" },\n" +
            "    { \"step\": 2, \"phoneme\": \"ㅏ\", \"syllable\": \"감\" },\n" +
            "    { \"step\": 3, \"phoneme\": \"ㅁ\", \"syllable\": \"감\" },\n" +
            "    { \"step\": 4, \"phoneme\": \"ㄱ\", \"syllable\": \"각\" },\n" +
            "    { \"step\": 5, \"phoneme\": \"ㅏ\", \"syllable\": \"각\" },\n" +
            "    { \"step\": 6, \"phoneme\": \"ㄱ\", \"syllable\": \"각\" }\n" +
            "  ],\n" +
            "  \"learningTips\": {\n" +
            "    \"commonMistakes\": [\"ㅁ 받침을 빼먹기 쉬워요\", \"두 번째 ㄱ 받침을 크게 쓰기 쉬워요\"],\n" +
            "    \"practiceWords\": [\"강각\", \"감정\", \"각도\"],\n" +
            "    \"rhymingWords\": [\"암각\", \"담각\"]\n" +
            "  }\n" +
            "}\n"
        , word);
    }
} 