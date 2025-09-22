package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.VocabularyBlockRequestDto;
import com.dyslexia.dyslexia.dto.VocabularyItemDto;
import com.dyslexia.dyslexia.service.VocabularyCallbackService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = VocabularyCallbackController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
    })
@TestPropertySource(properties = {
    "external.callback.token=test-token"
})
class VocabularyCallbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VocabularyCallbackService vocabularyCallbackService;

    @Test
    @DisplayName("블록 콜백 API 성공 테스트")
    void handleBlock_성공() throws Exception {
        // Given
        VocabularyBlockRequestDto request = createSampleBlockRequest();
        when(vocabularyCallbackService.handleBlockCallback(any())).thenReturn(2);

        // When & Then
        mockMvc.perform(post("/v1/ai/vocabulary/block")
                .header("X-Callback-Token", "test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.jobId").value("abc-123"))
                .andExpect(jsonPath("$.blockId").value("b-12"))
                .andExpect(jsonPath("$.saved").value(2));
    }

    @Test
    @DisplayName("블록 콜백 API - 토큰 없음 (401)")
    void handleBlock_토큰_없음() throws Exception {
        // Given
        VocabularyBlockRequestDto request = createSampleBlockRequest();

        // When & Then
        mockMvc.perform(post("/v1/ai/vocabulary/block")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("invalid token"));
    }

    @Test
    @DisplayName("블록 콜백 API - 잘못된 토큰 (401)")
    void handleBlock_잘못된_토큰() throws Exception {
        // Given
        VocabularyBlockRequestDto request = createSampleBlockRequest();

        // When & Then
        mockMvc.perform(post("/v1/ai/vocabulary/block")
                .header("X-Callback-Token", "wrong-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("블록 콜백 API - jobId 누락 (400)")
    void handleBlock_jobId_누락() throws Exception {
        // Given
        VocabularyBlockRequestDto request = createSampleBlockRequest();
        request.setJobId(null);

        // When & Then
        mockMvc.perform(post("/v1/ai/vocabulary/block")
                .header("X-Callback-Token", "test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value("MISSING_JOB_ID"));
    }

    @Test
    @DisplayName("블록 콜백 API - textbookId 누락 (400)")
    void handleBlock_textbookId_누락() throws Exception {
        // Given
        VocabularyBlockRequestDto request = createSampleBlockRequest();
        request.setTextbookId(null);

        // When & Then
        mockMvc.perform(post("/v1/ai/vocabulary/block")
                .header("X-Callback-Token", "test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value("MISSING_TEXTBOOK_ID"));
    }

    @Test
    @DisplayName("블록 콜백 API - blockId 누락 (400)")
    void handleBlock_blockId_누락() throws Exception {
        // Given
        VocabularyBlockRequestDto request = createSampleBlockRequest();
        request.setBlockId(null);

        // When & Then
        mockMvc.perform(post("/v1/ai/vocabulary/block")
                .header("X-Callback-Token", "test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value("MISSING_BLOCK_ID"));
    }

    @Test
    @DisplayName("블록 콜백 API - vocabulary_items 없음 (422)")
    void handleBlock_vocabulary_items_없음() throws Exception {
        // Given
        VocabularyBlockRequestDto request = createSampleBlockRequest();
        request.setVocabularyItems(List.of());

        // When & Then
        mockMvc.perform(post("/v1/ai/vocabulary/block")
                .header("X-Callback-Token", "test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value("INVALID_ITEMS_COUNT"));
    }

    @Test
    @DisplayName("블록 콜백 API - vocabulary_items 6개 초과 (422)")
    void handleBlock_vocabulary_items_6개_초과() throws Exception {
        // Given
        VocabularyBlockRequestDto request = createSampleBlockRequest();
        // 6개 항목 추가
        List<VocabularyItemDto> items = List.of(
            createVocabularyItem("단어1", 0, 2),
            createVocabularyItem("단어2", 3, 5),
            createVocabularyItem("단어3", 6, 8),
            createVocabularyItem("단어4", 9, 11),
            createVocabularyItem("단어5", 12, 14),
            createVocabularyItem("단어6", 15, 17)
        );
        request.setVocabularyItems(items);

        // When & Then
        mockMvc.perform(post("/v1/ai/vocabulary/block")
                .header("X-Callback-Token", "test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value("INVALID_ITEMS_COUNT"));
    }

    private VocabularyBlockRequestDto createSampleBlockRequest() {
        VocabularyBlockRequestDto request = new VocabularyBlockRequestDto();
        request.setJobId("abc-123");
        request.setTextbookId(7L);
        request.setBlockId("b-12");
        request.setPageNumber(1);
        request.setOriginalSentence("전자영수증을 확인했어요.");
        request.setCreatedAt("2025-01-15T12:35:12Z");

        VocabularyItemDto item1 = createVocabularyItem("영수증", 2, 5);
        VocabularyItemDto item2 = createVocabularyItem("확인", 6, 8);

        request.setVocabularyItems(List.of(item1, item2));
        return request;
    }

    private VocabularyItemDto createVocabularyItem(String word, int start, int end) {
        VocabularyItemDto item = new VocabularyItemDto();
        item.setWord(word);
        item.setStartIndex(start);
        item.setEndIndex(end);
        item.setDefinition(word + "의 정의");
        item.setPhonemeAnalysisJson("{\"phonemes\": [\"test\"], \"syllables\": 2}");
        return item;
    }
}