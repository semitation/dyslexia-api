package com.dyslexia.dyslexia.domain.pdf;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyAnalysisRepository extends JpaRepository<VocabularyAnalysis, Long> {
    List<VocabularyAnalysis> findByTextbookId(Long textbookId);
    List<VocabularyAnalysis> findByTextbookIdAndPageNumber(Long textbookId, Integer pageNumber);
    List<VocabularyAnalysis> findByTextbookIdAndPageNumberAndBlockId(Long textbookId, Integer pageNumber, String blockId);

    java.util.Optional<VocabularyAnalysis> findByTextbookIdAndBlockIdAndWordAndStartIndexAndEndIndex(
        Long textbookId,
        String blockId,
        String word,
        Integer startIndex,
        Integer endIndex
    );

    // Document 기반 조회(교재 ID를 모를 때)
    @Query(value = "select va.* from vocabulary_analysis va join textbooks t on t.id = va.textbook_id where t.document_id = :documentId", nativeQuery = true)
    List<VocabularyAnalysis> findByDocumentId(@Param("documentId") Long documentId);

    @Query(value = "select va.* from vocabulary_analysis va join textbooks t on t.id = va.textbook_id where t.document_id = :documentId and va.page_number = :pageNumber", nativeQuery = true)
    List<VocabularyAnalysis> findByDocumentIdAndPageNumber(@Param("documentId") Long documentId, @Param("pageNumber") Integer pageNumber);

    @Query(value = "select va.* from vocabulary_analysis va join textbooks t on t.id = va.textbook_id where t.document_id = :documentId and va.page_number = :pageNumber and va.block_id = :blockId", nativeQuery = true)
    List<VocabularyAnalysis> findByDocumentIdAndPageNumberAndBlockId(@Param("documentId") Long documentId, @Param("pageNumber") Integer pageNumber, @Param("blockId") String blockId);
} 
