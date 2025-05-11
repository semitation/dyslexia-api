package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.entity.Page;
import com.dyslexia.dyslexia.entity.PageImage;
import com.dyslexia.dyslexia.entity.PageTip;
import com.dyslexia.dyslexia.repository.DocumentRepository;
import com.dyslexia.dyslexia.repository.PageImageRepository;
import com.dyslexia.dyslexia.repository.PageRepository;
import com.dyslexia.dyslexia.repository.PageTipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentContentService {

    private final DocumentRepository documentRepository;
    private final PageRepository pageRepository;
    private final PageTipRepository pageTipRepository;
    private final PageImageRepository pageImageRepository;

    public List<Page> getPagesByDocumentId(Long documentId, Integer pageNumber) {
        if (pageNumber != null) {
            return documentRepository.findById(documentId)
                    .map(document -> pageRepository.findByDocumentAndPageNumber(document, pageNumber)
                            .map(List::of)
                            .orElse(List.of()))
                    .orElse(List.of());
        }
        return pageRepository.findByDocumentIdOrderByPageNumberAsc(documentId);
    }

    public List<PageTip> getPageTipsByPageId(Long pageId) {
        return pageTipRepository.findByPageId(pageId);
    }

    public List<PageImage> getPageImagesByPageId(Long pageId) {
        return pageImageRepository.findByPageId(pageId);
    }
} 