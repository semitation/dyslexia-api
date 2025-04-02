package com.dyslexia.dyslexia.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.dyslexia.dyslexia.dto.OpenAIResponse;

import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.domain.chat.ChatMessage.SystemMessage;
import io.github.sashirestela.openai.domain.chat.ChatMessage.UserMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OpenAIService {
    
    @Value("${OPENAI_API_KEY}")
    private String openAIKey;

    private String openaiModel = "o3-mini";

    public OpenAIResponse generateText(String prompt) {
        try {
            log.info("OpenAI API 호출 시작 - 프롬프트 길이: {}", prompt.length());

            String apiKey = openAIKey;
            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.warn("OpenAI API 키를 찾을 수 없습니다");
                throw new IllegalStateException("API 키가 설정되지 않았습니다");
            }
            log.debug("API 키 유효성 확인: {}", apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
            
            SimpleOpenAI openAI = SimpleOpenAI.builder()
                    .apiKey(apiKey)
                    .build();

            SystemMessage systemMessage = SystemMessage.of(
                    "[입력 데이터]\n- 제공된 문서(PDF에서 추출된 텍스트)\n\n[대상]\n- 대상: 난독증 아동 (읽기와 이해에 어려움을 겪는 학생)\n- 연령/학습 수준: 초등학교 고학년 또는 중학생\n\n[목표 및 변환 기준]\n- 텍스트를 쉽고 명료하게 재작성하여 단문 위주로 구성할 것.\n- 어려운 단어와 비유적 표현은 사용하지 않고, 필요한 경우 핵심 용어에 간단한 설명(예: 괄호 안에 쉬운 단어 설명)을 추가할 것.\n- 문맥을 고려해 전체 교과서의 흐름과 연결되도록 할 것.\n- 중요한 정보는 누락 없이 전달할 것.\n- 핵심 개념은 굵은 글씨로 강조할 것.\n- 추상적인 개념에는 구체적 예시를 추가할 것.\n- 개념 설명에 필요한 시각화 요소를 제안할 것.\n\n[출력 형식]\n- 마크다운 형식\n- 단원의 개요\n- 단원 목차(핵심 콜아웃 포인트)\n- 단원 내용(각 콜아웃에 대한 설명 문구)\n- 변환 과정에서의 주의 사항이나 추가 설명");

            ChatRequest chatRequest = ChatRequest.builder()
                    .model(openaiModel) // o3-mini
                    .message(systemMessage)
                    .message(UserMessage.of(prompt))
                    .temperature(0.7)
                    .build();
            log.info("OpenAI API 요청 생성 완료: model" + openaiModel);

            try {
                var futureChat = openAI.chatCompletions().create(chatRequest);
                log.info("OpenAI API 호출 완료, 응답 대기 중");
                var chatResponse = futureChat.join();
                log.info("OpenAI API 응답 수신 완료");
                String content = chatResponse.firstContent();
                log.info("응답 내용 길이: {}", content.length());

                return OpenAIResponse.builder()
                        .output(content)
                        .message("OpenAI API 응답 처리 완료")
                        .build();
            } catch (Exception e) {
                log.error("OpenAI API 응답 처리 중 오류: {}", e.getMessage(), e);
                throw e;
            }
            
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage(), e);
            return OpenAIResponse.builder()
                    .output("")
                    .message("오류 발생: " + e.getMessage())
                    .build();
        }
    }

    public void setOpenaiModel(String openaiModel) {
        this.openaiModel = openaiModel;
    }
}
