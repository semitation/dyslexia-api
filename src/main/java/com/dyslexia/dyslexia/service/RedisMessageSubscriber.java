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
            log.info("결과 메시지 수신: {}", message);

            ResultMessageDto resultMessage = objectMapper.readValue(message, ResultMessageDto.class);

            Optional<Document> documentOpt = documentRepository.findByJobId(resultMessage.getJobId());

            if (documentOpt.isPresent()) {
                Document document = documentOpt.get();

                JsonNode resultJson = s3JsonProcessingService.downloadAndParseJson(resultMessage.getS3Url());

                documentDataProcessingService.processAndSaveDocumentData(document, resultJson);

                document.setProcessingStatus(DocumentProcessingStatus.COMPLETED);
                documentRepository.save(document);

                log.info("교안 생성 완료. JobId: {}", resultMessage.getJobId());

            } else {
                log.warn("결과 처리 대상 문서를 찾을 수 없음. JobId: {}", resultMessage.getJobId());
            }

        } catch (Exception e) {
            log.error("결과 메시지 처리 중 오류 발생: {}", message, e);

            try {
                ResultMessageDto resultMessage = objectMapper.readValue(message, ResultMessageDto.class);
                Optional<Document> documentOpt = documentRepository.findByJobId(resultMessage.getJobId());

                if (documentOpt.isPresent()) {
                    Document document = documentOpt.get();
                    document.setProcessingStatus(DocumentProcessingStatus.FAILED);
                    document.setErrorMessage("결과 처리 중 오류: " + e.getMessage());
                    documentRepository.save(document);
                }
            } catch (Exception ex) {
                log.error("결과 메시지 처리 실패 상태 업데이트 중 오류", ex);
            }
        }
    }

    public void handleFailureMessage(String message) {
        try {
            log.info("실패 메시지 수신: {}", message);

            FailureMessageDto failureMessage = objectMapper.readValue(message, FailureMessageDto.class);

            Optional<Document> documentOpt = documentRepository.findByJobId(failureMessage.getJobId());

            if (documentOpt.isPresent()) {
                Document document = documentOpt.get();
                document.setProcessingStatus(DocumentProcessingStatus.FAILED);
                document.setErrorMessage(failureMessage.getError());
                documentRepository.save(document);

                log.info("실패 상태 업데이트 완료. JobId: {}, Error: {}",
                        failureMessage.getJobId(), failureMessage.getError());
            } else {
                log.warn("실패 처리 대상 문서를 찾을 수 없음. JobId: {}", failureMessage.getJobId());
            }

        } catch (Exception e) {
            log.error("실패 메시지 처리 중 오류 발생: {}", message, e);
        }
    }
}