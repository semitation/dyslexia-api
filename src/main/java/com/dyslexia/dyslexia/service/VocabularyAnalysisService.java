package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.domain.pdf.VocabularyAnalysis;
import com.dyslexia.dyslexia.domain.pdf.VocabularyAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VocabularyAnalysisService {

    private final VocabularyAnalysisRepository vocabularyAnalysisRepository;

    @Transactional(readOnly = true)
    public List<VocabularyAnalysis> searchVocabularyAnalysis(Long textbookId, Integer pageNumber, String blockId) {
        log.debug("어휘 분석 검색: textbookId={}, pageNumber={}, blockId={}", textbookId, pageNumber, blockId);
        
        if (blockId != null && !blockId.isEmpty()) {
            return vocabularyAnalysisRepository.findByTextbookIdAndPageNumberAndBlockId(textbookId, pageNumber, blockId);
        } else if (pageNumber != null) {
            return vocabularyAnalysisRepository.findByTextbookIdAndPageNumber(textbookId, pageNumber);
        } else {
            return vocabularyAnalysisRepository.findByTextbookId(textbookId);
        }
    }
} 