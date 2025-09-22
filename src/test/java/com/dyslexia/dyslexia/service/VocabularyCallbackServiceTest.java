package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.domain.pdf.VocabularyAnalysis;
import com.dyslexia.dyslexia.domain.pdf.VocabularyAnalysisRepository;
import com.dyslexia.dyslexia.dto.VocabularyBlockRequestDto;
import com.dyslexia.dyslexia.dto.VocabularyItemDto;
import com.dyslexia.dyslexia.entity.Document;
import com.dyslexia.dyslexia.entity.Textbook;
import com.dyslexia.dyslexia.repository.DocumentRepository;
import com.dyslexia.dyslexia.repository.TextbookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VocabularyCallbackServiceTest {

    @Mock
    private VocabularyAnalysisRepository vocabularyAnalysisRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private TextbookRepository textbookRepository;

    @InjectMocks
    private VocabularyCallbackService vocabularyCallbackService;

    @Test
    @DisplayName("블록 콜백 처리 성공 - 새로운 항목 저장")
    void handleBlockCallback_성공_새로운_항목_저장() {
        // Given
        VocabularyBlockRequestDto request = createSampleBlockRequest();

        when(vocabularyAnalysisRepository.findByTextbookIdAndBlockIdAndWordAndStartIndexAndEndIndex(
            any(), any(), any(), any(), any()))
            .thenReturn(Optional.empty());
        when(vocabularyAnalysisRepository.save(any(VocabularyAnalysis.class)))
            .thenReturn(new VocabularyAnalysis());

        // When
        int result = vocabularyCallbackService.handleBlockCallback(request);

        // Then
        assertThat(result).isEqualTo(2); // 2개 항목 저장
        verify(vocabularyAnalysisRepository, times(2)).save(any(VocabularyAnalysis.class));
    }

    @Test
    @DisplayName("블록 콜백 처리 성공 - 기존 항목 업데이트")
    void handleBlockCallback_성공_기존_항목_업데이트() {
        // Given
        VocabularyBlockRequestDto request = createSampleBlockRequest();
        VocabularyAnalysis existingAnalysis = VocabularyAnalysis.builder()
            .id(1L)
            .textbookId(7L)
            .blockId("b-12")
            .word("영수증")
            .build();

        when(vocabularyAnalysisRepository.findByTextbookIdAndBlockIdAndWordAndStartIndexAndEndIndex(
            any(), any(), any(), any(), any()))
            .thenReturn(Optional.of(existingAnalysis));
        when(vocabularyAnalysisRepository.save(any(VocabularyAnalysis.class)))
            .thenReturn(existingAnalysis);

        // When
        int result = vocabularyCallbackService.handleBlockCallback(request);

        // Then
        assertThat(result).isEqualTo(2); // 2개 항목 업데이트
        verify(vocabularyAnalysisRepository, times(2)).save(any(VocabularyAnalysis.class));
    }

    @Test
    @DisplayName("어휘 항목 수가 5개 초과시 상위 5개만 처리")
    void handleBlockCallback_어휘_항목_5개_초과시_상위_5개만_처리() {
        // Given
        VocabularyBlockRequestDto request = createBlockRequestWithManyItems();

        when(vocabularyAnalysisRepository.findByTextbookIdAndBlockIdAndWordAndStartIndexAndEndIndex(
            any(), any(), any(), any(), any()))
            .thenReturn(Optional.empty());
        when(vocabularyAnalysisRepository.save(any(VocabularyAnalysis.class)))
            .thenReturn(new VocabularyAnalysis());

        // When
        int result = vocabularyCallbackService.handleBlockCallback(request);

        // Then
        assertThat(result).isEqualTo(5); // 최대 5개만 처리
        verify(vocabularyAnalysisRepository, times(5)).save(any(VocabularyAnalysis.class));
    }

    @Test
    @DisplayName("잘못된 인덱스 보정 테스트")
    void handleBlockCallback_잘못된_인덱스_보정() {
        // Given
        VocabularyBlockRequestDto request = createRequestWithInvalidIndexes();

        when(vocabularyAnalysisRepository.findByTextbookIdAndBlockIdAndWordAndStartIndexAndEndIndex(
            any(), any(), any(), any(), any()))
            .thenReturn(Optional.empty());
        when(vocabularyAnalysisRepository.save(any(VocabularyAnalysis.class)))
            .thenReturn(new VocabularyAnalysis());

        // When
        int result = vocabularyCallbackService.handleBlockCallback(request);

        // Then
        assertThat(result).isEqualTo(1);
        verify(vocabularyAnalysisRepository).save(any(VocabularyAnalysis.class));
    }

    private VocabularyBlockRequestDto createSampleBlockRequest() {
        VocabularyBlockRequestDto request = new VocabularyBlockRequestDto();
        request.setJobId("abc-123");
        request.setTextbookId(7L);
        request.setBlockId("b-12");
        request.setPageNumber(1);
        request.setOriginalSentence("전자영수증을 확인했어요.");
        request.setCreatedAt("2025-01-15T12:35:12Z");

        VocabularyItemDto item1 = new VocabularyItemDto();
        item1.setWord("영수증");
        item1.setStartIndex(2);
        item1.setEndIndex(5);
        item1.setDefinition("물품을 사고 받는 증명서");

        VocabularyItemDto item2 = new VocabularyItemDto();
        item2.setWord("확인");
        item2.setStartIndex(6);
        item2.setEndIndex(8);
        item2.setDefinition("분명히 알아보는 것");

        request.setVocabularyItems(List.of(item1, item2));
        return request;
    }

    private VocabularyBlockRequestDto createBlockRequestWithManyItems() {
        VocabularyBlockRequestDto request = new VocabularyBlockRequestDto();
        request.setJobId("abc-123");
        request.setTextbookId(7L);
        request.setBlockId("b-13");
        request.setPageNumber(1);
        request.setOriginalSentence("많은 어휘 항목이 포함된 긴 문장입니다.");

        // 7개 항목 생성 (5개 초과)
        List<VocabularyItemDto> items = List.of(
            createVocabularyItem("많은", 0, 2),
            createVocabularyItem("어휘", 3, 5),
            createVocabularyItem("항목", 6, 8),
            createVocabularyItem("포함", 11, 13),
            createVocabularyItem("긴", 16, 17),
            createVocabularyItem("문장", 18, 20),
            createVocabularyItem("입니다", 20, 23)
        );

        request.setVocabularyItems(items);
        return request;
    }

    private VocabularyBlockRequestDto createRequestWithInvalidIndexes() {
        VocabularyBlockRequestDto request = new VocabularyBlockRequestDto();
        request.setJobId("abc-123");
        request.setTextbookId(7L);
        request.setBlockId("b-14");
        request.setPageNumber(1);
        request.setOriginalSentence("테스트 문장입니다.");

        VocabularyItemDto item = new VocabularyItemDto();
        item.setWord("테스트");
        item.setStartIndex(5); // 잘못된 인덱스 (end < start)
        item.setEndIndex(2);
        item.setDefinition("시험해보는 것");

        request.setVocabularyItems(List.of(item));
        return request;
    }

    private VocabularyItemDto createVocabularyItem(String word, int start, int end) {
        VocabularyItemDto item = new VocabularyItemDto();
        item.setWord(word);
        item.setStartIndex(start);
        item.setEndIndex(end);
        item.setDefinition(word + "의 정의");
        item.setPhonemeAnalysisJson("{\"phonemes\": [\"test\"], \"word\": \"" + word + "\"}");
        return item;
    }
}