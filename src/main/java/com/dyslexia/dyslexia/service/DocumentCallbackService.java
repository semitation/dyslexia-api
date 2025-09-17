package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.DocumentCompleteRequestDto;
import com.dyslexia.dyslexia.entity.Document;
import com.dyslexia.dyslexia.entity.DocumentResult;
import com.dyslexia.dyslexia.enums.DocumentProcessingStatus;
import com.dyslexia.dyslexia.repository.DocumentRepository;
import com.dyslexia.dyslexia.repository.DocumentResultRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentCallbackService {

    private final DocumentRepository documentRepository;
    private final DocumentResultRepository documentResultRepository;
    private final DocumentDataProcessingService documentDataProcessingService;

    @Transactional
    public boolean handleCompletion(DocumentCompleteRequestDto request) {
        final String jobId = request.getJobId();

        if (documentResultRepository.existsByJobId(jobId)) {
            log.info("중복 콜백 수신 - 이미 처리됨. JobId: {}", jobId);
            return false; // duplicate
        }

        JsonNode data = request.getData();
        String rawJson = data.toString();

        DocumentResult result = new DocumentResult(jobId, request.getPdfName(), rawJson);
        documentResultRepository.save(result);

        // Kick off async processing so we can ack quickly
        processAsync(jobId, data);

        log.info("문서 완료 콜백 처리 완료. JobId: {}", jobId);
        return true;
    }

    @org.springframework.scheduling.annotation.Async
    public void processAsync(String jobId, JsonNode data) {
        try {
            documentRepository.findByJobId(jobId).ifPresentOrElse(document -> {
                try {
                    documentDataProcessingService.processAndSaveDocumentData(document, data);
                    document.setProgress(100);
                    document.setProcessingStatus(DocumentProcessingStatus.COMPLETED);
                    documentRepository.save(document);
                    log.info("문서 비동기 처리 완료. JobId: {}", jobId);
                } catch (Exception e) {
                    log.error("문서 비동기 처리 실패. JobId: {}", jobId, e);
                    documentDataProcessingService.markDocumentAsFailed(jobId, e.getMessage());
                }
            }, () -> log.warn("비동기 처리 대상 문서를 찾을 수 없음. JobId: {}", jobId));
        } catch (Exception e) {
            log.error("콜백 비동기 처리 스케줄링 실패. JobId: {}", jobId, e);
        }
    }
}
