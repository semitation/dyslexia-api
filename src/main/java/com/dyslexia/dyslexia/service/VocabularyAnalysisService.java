package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.domain.pdf.VocabularyAnalysis;
import com.dyslexia.dyslexia.domain.pdf.VocabularyAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VocabularyAnalysisService {

    private final VocabularyAnalysisRepository vocabularyAnalysisRepository;

    @Transactional(readOnly = true)
    public List<VocabularyAnalysis> searchVocabularyAnalysis(Long textbookId, Long documentId, Integer pageNumber, String blockId) {
        log.info("=== VocabularyAnalysisService 검색 실행 ===");
        log.info("검색 조건: textbookId={}, documentId={}, pageNumber={}, blockId={}", textbookId, documentId, pageNumber, blockId);

        List<VocabularyAnalysis> results = Collections.emptyList();

        if (textbookId != null) {
            if (blockId != null && !blockId.isEmpty() && pageNumber != null) {
                results = vocabularyAnalysisRepository.findByTextbookIdAndPageNumberAndBlockId(textbookId, pageNumber, blockId);
            } else if (pageNumber != null) {
                results = vocabularyAnalysisRepository.findByTextbookIdAndPageNumber(textbookId, pageNumber);
            } else {
                results = vocabularyAnalysisRepository.findByTextbookId(textbookId);
            }
        } else if (documentId != null) {
            if (blockId != null && !blockId.isEmpty() && pageNumber != null) {
                results = vocabularyAnalysisRepository.findByDocumentIdAndPageNumberAndBlockId(documentId, pageNumber, blockId);
            } else if (pageNumber != null) {
                results = vocabularyAnalysisRepository.findByDocumentIdAndPageNumber(documentId, pageNumber);
            } else {
                results = vocabularyAnalysisRepository.findByDocumentId(documentId);
            }
        }

        int count = results != null ? results.size() : 0;
        log.info("Repository 검색 결과: {}개", count);

        // Fallback: 범위를 점진적으로 완화하여 일부라도 반환
        if (count == 0) {
            if (textbookId != null) {
                if (blockId != null && pageNumber != null) {
                    log.info("Fallback 적용: textbookId + pageNumber로 재조회");
                    results = vocabularyAnalysisRepository.findByTextbookIdAndPageNumber(textbookId, pageNumber);
                    count = results != null ? results.size() : 0;
                }
                if (count == 0 && pageNumber != null) {
                    log.info("Fallback 적용: textbookId로 재조회");
                    results = vocabularyAnalysisRepository.findByTextbookId(textbookId);
                }
            } else if (documentId != null) {
                if (blockId != null && pageNumber != null) {
                    log.info("Fallback 적용: documentId + pageNumber로 재조회");
                    results = vocabularyAnalysisRepository.findByDocumentIdAndPageNumber(documentId, pageNumber);
                    count = results != null ? results.size() : 0;
                }
                if (count == 0 && pageNumber != null) {
                    log.info("Fallback 적용: documentId로 재조회");
                    results = vocabularyAnalysisRepository.findByDocumentId(documentId);
                }
            }
        }

        return results != null ? results : Collections.emptyList();
    }
}
