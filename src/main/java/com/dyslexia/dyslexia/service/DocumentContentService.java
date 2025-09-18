package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.PageContentResponseDto;
import com.dyslexia.dyslexia.entity.Page;
import com.dyslexia.dyslexia.entity.Textbook;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.exception.ApplicationException;
import com.dyslexia.dyslexia.exception.ExceptionCode;
import com.dyslexia.dyslexia.repository.PageRepository;
import com.dyslexia.dyslexia.repository.TextbookRepository;
import com.dyslexia.dyslexia.repository.GuardianRepository;
import com.dyslexia.dyslexia.util.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DocumentContentService {

    private final GuardianRepository guardianRepository;
    private final TextbookRepository textbookRepository;
    private final PageRepository pageRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    public List<PageContentResponseDto> getMyDocumentPages(Long documentId, Integer pageNumber) {
        String currentClientId = jwtTokenProvider.getCurrentClientId();
        Guardian guardian = guardianRepository.findByClientId(currentClientId)
            .orElseThrow(() -> new ApplicationException(ExceptionCode.GUARDIAN_NOT_FOUND));

        Textbook textbook = textbookRepository.findByDocumentIdAndGuardianId(documentId, guardian.getId())
            .orElseThrow(() -> new ApplicationException(ExceptionCode.ACCESS_DENIED));

        List<Page> pages;
        if (pageNumber == null) {
            pages = pageRepository.findByTextbookIdOrderByPageNumberAsc(textbook.getId());
        } else {
            pages = pageRepository.findByTextbookAndPageNumber(textbook, pageNumber)
                .map(List::of)
                .orElse(List.of());
        }

        return pages.stream()
            .map(page -> PageContentResponseDto.fromEntity(page, objectMapper))
            .toList();
    }
}

