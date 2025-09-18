package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.entity.Document;
import com.dyslexia.dyslexia.entity.Page;
import com.dyslexia.dyslexia.entity.Textbook;
import com.dyslexia.dyslexia.enums.DocumentProcessingStatus;
import com.dyslexia.dyslexia.enums.ConvertProcessStatus;
import com.dyslexia.dyslexia.exception.ApplicationException;
import com.dyslexia.dyslexia.exception.ExceptionCode;
import com.dyslexia.dyslexia.repository.DocumentRepository;
import com.dyslexia.dyslexia.repository.PageRepository;
import com.dyslexia.dyslexia.repository.TextbookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentDataProcessingService {

    private final TextbookRepository textbookRepository;
    private final PageRepository pageRepository;
    private final DocumentRepository documentRepository;

    @Transactional
    public void processAndSaveDocumentData(Document document, JsonNode resultJson) {
        try {
            log.info("문서 데이터 처리 시작. JobId: {}", document.getJobId());

            Textbook textbook = createTextbookFromDocument(document);
            textbookRepository.save(textbook);

            List<Page> pages = parseJsonToPages(resultJson, textbook);
            // If pipeline provides chunk-based content instead of pages
            if (pages.isEmpty()) {
                pages = parseContentChunksToPages(resultJson, textbook);
            }
            pageRepository.saveAll(pages);

            // 완료 상태로 마킹 및 카운트 반영
            textbook.setConvertProcessStatus(ConvertProcessStatus.COMPLETED);
            textbook.setLearnRate(0); // 초기값 유지 (필요 시 계산 로직으로 교체)
            textbook.setPageCount(pages.size());
            textbookRepository.save(textbook);

            log.info("문서 데이터 처리 완료. JobId: {}, Pages: {}",
                    document.getJobId(), pages.size());

        } catch (Exception e) {
            log.error("문서 데이터 처리 중 오류 발생. JobId: {}", document.getJobId(), e);
            throw new RuntimeException("문서 데이터 처리 실패", e);
        }
    }

    /**
     * Job ID에 해당하는 Document의 상태를 FAILED로 변경하고 에러 메시지를 기록한다.
     * 실패 시점을 completedAt에 기록한다.
     *
     * @param jobId 작업 식별자
     * @param errorMessage 실패 사유 메시지
     */
    @Transactional
    public void markDocumentAsFailed(String jobId, String errorMessage) {
        Document document = documentRepository.findByJobId(jobId)
            .orElseThrow(() -> new ApplicationException(ExceptionCode.ENTITY_NOT_FOUND));

        log.warn("Document 상태를 FAILED로 변경합니다. Job ID: {}, Error: {}", jobId, errorMessage);

        document.setProcessingStatus(DocumentProcessingStatus.FAILED);
        document.setErrorMessage(errorMessage);
        document.setCompletedAt(LocalDateTime.now());

        documentRepository.save(document);
    }

    private Textbook createTextbookFromDocument(Document document) {
        // 이미 업로드 시점에 프리뷰(Textbook)가 생성되어 있을 수 있음. 있으면 재사용.
        return textbookRepository.findByDocumentId(document.getId())
                .orElseGet(() -> Textbook.builder()
                        .document(document)
                        .guardian(document.getGuardian())
                        .title(document.getTitle())
                        .build());
    }

    private List<Page> parseJsonToPages(JsonNode resultJson, Textbook textbook) {
        List<Page> pages = new ArrayList<>();

        if (resultJson.has("pages") && resultJson.get("pages").isArray()) {
            JsonNode pagesNode = resultJson.get("pages");

            for (JsonNode pageNode : pagesNode) {
                Page page = createPageFromJsonNode(pageNode, textbook);
                pages.add(page);
            }
        }

        return pages;
    }

    private List<Page> parseContentChunksToPages(JsonNode resultJson, Textbook textbook) {
        List<Page> pages = new ArrayList<>();

        if (resultJson.has("content") && resultJson.get("content").isArray()) {
            JsonNode contentArray = resultJson.get("content");

            int idx = 0;
            for (JsonNode chunkNode : contentArray) {
                Integer chunkIndex = chunkNode.has("chunk_index") ? chunkNode.get("chunk_index").asInt() : idx;
                String originalText = chunkNode.has("text") ? chunkNode.get("text").asText() : null;

                Page page = Page.builder()
                    .textbook(textbook)
                    .pageNumber(chunkIndex)
                    .originalContent(originalText)
                    .processedContent(chunkNode)
                    .sectionTitle(null)
                    .readingLevel(null)
                    .wordCount(originalText != null ? originalText.length() : null)
                    .complexityScore(null)
                    .processingStatus(ConvertProcessStatus.COMPLETED)
                    .build();

                pages.add(page);
                idx++;
            }
        }

        return pages;
    }

    private Page createPageFromJsonNode(JsonNode pageNode, Textbook textbook) {
        Integer pageNumber = pageNode.has("page_number") ?
                pageNode.get("page_number").asInt() : 0;

        String originalContent = pageNode.has("original_content") ?
                pageNode.get("original_content").asText() : null;

        String sectionTitle = pageNode.has("section_title") ?
                pageNode.get("section_title").asText() : null;

        Integer readingLevel = pageNode.has("reading_level") ?
                pageNode.get("reading_level").asInt() : null;

        Integer wordCount = pageNode.has("word_count") ?
                pageNode.get("word_count").asInt() : null;

        Float complexityScore = pageNode.has("complexity_score") ?
                (float) pageNode.get("complexity_score").asDouble() : null;

        return Page.builder()
                .textbook(textbook)
                .pageNumber(pageNumber)
                .originalContent(originalContent)
                .processedContent(pageNode)
                .sectionTitle(sectionTitle)
                .readingLevel(readingLevel)
                .wordCount(wordCount)
                .complexityScore(complexityScore)
                .processingStatus(ConvertProcessStatus.COMPLETED)
                .build();
    }
}
