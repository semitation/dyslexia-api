package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.entity.Document;
import com.dyslexia.dyslexia.entity.Page;
import com.dyslexia.dyslexia.entity.Textbook;
import com.dyslexia.dyslexia.repository.PageRepository;
import com.dyslexia.dyslexia.repository.TextbookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentDataProcessingService {

    private final TextbookRepository textbookRepository;
    private final PageRepository pageRepository;

    @Transactional
    public void processAndSaveDocumentData(Document document, JsonNode resultJson) {
        try {
            log.info("문서 데이터 처리 시작. JobId: {}", document.getJobId());

            Textbook textbook = createTextbookFromDocument(document);
            textbookRepository.save(textbook);

            List<Page> pages = parseJsonToPages(resultJson, textbook);
            pageRepository.saveAll(pages);

            log.info("문서 데이터 처리 완료. JobId: {}, Pages: {}",
                    document.getJobId(), pages.size());

        } catch (Exception e) {
            log.error("문서 데이터 처리 중 오류 발생. JobId: {}", document.getJobId(), e);
            throw new RuntimeException("문서 데이터 처리 실패", e);
        }
    }

    private Textbook createTextbookFromDocument(Document document) {
        return Textbook.builder()
                .title(document.getTitle())
                .fileName(document.getOriginalFilename())
                .fileSize(document.getFileSize())
                .uploadedBy(document.getGuardian())
                .build();
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
                .build();
    }
}