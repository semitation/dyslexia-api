package com.dyslexia.dyslexia.domain.pdf;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyAnalysisRepository extends JpaRepository<VocabularyAnalysis, Long> {
    List<VocabularyAnalysis> findByTextbookId(Long textbookId);
    List<VocabularyAnalysis> findByTextbookIdAndPageNumber(Long textbookId, Integer pageNumber);
    List<VocabularyAnalysis> findByTextbookIdAndPageNumberAndBlockId(Long textbookId, Integer pageNumber, String blockId);
} 