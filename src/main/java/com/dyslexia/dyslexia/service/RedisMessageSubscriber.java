package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.FailureMessageDto;
import com.dyslexia.dyslexia.dto.ProgressMessageDto;
import com.dyslexia.dyslexia.dto.ResultMessageDto;
import com.dyslexia.dyslexia.entity.Document;
import com.dyslexia.dyslexia.enums.DocumentProcessingStatus;
import com.dyslexia.dyslexia.repository.DocumentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessageSubscriber {

    private final DocumentRepository documentRepository;
    private final S3JsonProcessingService s3JsonProcessingService;
    private final DocumentDataProcessingService documentDataProcessingService;
    private final ObjectMapper objectMapper;

    public void handleProgressMessage(String message) {
        try {
            log.info("진행률 메시지 수신: {}", message);

            ProgressMessageDto progressMessage = objectMapper.readValue(message, ProgressMessageDto.class);

            Optional<Document> documentOpt = documentRepository.findByJobId(progressMessage.getJobId());

            if (documentOpt.isPresent()) {
                Document document = documentOpt.get();
                document.setProgress(progressMessage.getProgress());
                documentRepository.save(document);

                log.info("진행률 업데이트 완료. JobId: {}, Progress: {}%",
                        progressMessage.getJobId(), progressMessage.getProgress());
            } else {
                log.warn("진행률 업데이트 대상 문서를 찾을 수 없음. JobId: {}", progressMessage.getJobId());
            }

        } catch (Exception e) {
            log.error("진행률 메시지 처리 중 오류 발생: {}", message, e);
        }
    }

    @Transactional
    public void handleResultMessage(String message) {
        try {
            log.info("결과 메시지 수신(웹훅 우선이므로 S3 처리 생략): {}", message);

            ResultMessageDto resultMessage = objectMapper.readValue(message, ResultMessageDto.class);

            // 웹훅이 최종 결과를 전달하므로, Redis의 결과 메시지에서는 상태를 변경하지 않는다.
            // 필요 시 진행률만 100으로 보정하여 UI 반영을 돕는다.
            Optional<Document> documentOpt = documentRepository.findByJobId(resultMessage.getJobId());
            documentOpt.ifPresent(document -> {
                Integer progress = document.getProgress();
                if (progress == null || progress < 100) {
                    document.setProgress(100);
                    documentRepository.save(document);
                    log.info("진행률 100% 반영(결과 채널). JobId: {}", resultMessage.getJobId());
                }
            });

        } catch (Exception e) {
            // 결과 채널은 보조 신호이므로 실패로 상태를 바꾸지 않는다. 단순 로깅만 수행.
            log.error("결과 메시지 파싱 중 오류 (무시): {}", message, e);
        }
    }

    public void handleFailureMessage(String message) {
        try {
            log.info("실패 메시지 수신: {}", message);

            FailureMessageDto failureMessage = objectMapper.readValue(message, FailureMessageDto.class);

            log.info("FastAPI 처리 실패 메시지 수신: Job ID - {}", failureMessage.getJobId());

            documentDataProcessingService.markDocumentAsFailed(
                failureMessage.getJobId(),
                failureMessage.getErrorMessage()
            );

        } catch (Exception e) {
            log.error("실패 메시지 처리 중 오류 발생: {}", message, e);
        }
    }
}
