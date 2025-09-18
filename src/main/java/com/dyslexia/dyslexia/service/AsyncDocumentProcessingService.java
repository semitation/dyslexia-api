package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.AsyncDocumentCreateResponseDto;
import com.dyslexia.dyslexia.dto.DocumentProcessingStatusDto;
import com.dyslexia.dyslexia.entity.Document;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.entity.Textbook;
import com.dyslexia.dyslexia.enums.ConvertProcessStatus;
import com.dyslexia.dyslexia.enums.DocumentProcessingStatus;
import com.dyslexia.dyslexia.repository.DocumentRepository;
import com.dyslexia.dyslexia.repository.TextbookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncDocumentProcessingService {

    private final DocumentRepository documentRepository;
    private final TextbookRepository textbookRepository;
    private final FastApiClient fastApiClient;

    @Transactional
    public AsyncDocumentCreateResponseDto processDocumentAsync(Guardian guardian, MultipartFile file) {
        String jobId = generateJobId();

        Document document = Document.builder()
                .guardian(guardian)
                .title(extractTitle(file.getOriginalFilename()))
                .originalFilename(file.getOriginalFilename())
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .jobId(jobId)
                .build();

        documentRepository.save(document);

        // 1) 업로드 즉시 교재 프리뷰(placeholder) 엔티티 생성 → 목록 노출 가능
        Textbook textbook = Textbook.builder()
            .document(document)
            .guardian(guardian)
            .title(document.getTitle())
            .pageCount(null)
            .learnRate(0)
            .build();
        textbook.setConvertProcessStatus(ConvertProcessStatus.PENDING);
        textbookRepository.save(textbook);

        try {
            fastApiClient.processDocumentAsync(file, jobId);
            document.setProcessingStatus(DocumentProcessingStatus.PROCESSING);
            documentRepository.save(document);

            // 교재 상태도 PROCESSING으로 전환
            textbook.setConvertProcessStatus(ConvertProcessStatus.PROCESSING);
            textbookRepository.save(textbook);

            log.info("비동기 교안 생성 요청 완료. JobId: {}, FileName: {}", jobId, file.getOriginalFilename());

            return AsyncDocumentCreateResponseDto.builder()
                    .jobId(jobId)
                    .message("교안 생성이 시작되었습니다.")
                    .build();

        } catch (Exception e) {
            document.setProcessingStatus(DocumentProcessingStatus.FAILED);
            document.setErrorMessage(e.getMessage());
            documentRepository.save(document);

            // 교재 상태 실패로 전환 (프리뷰 엔티티도 함께 표시)
            textbook.setConvertProcessStatus(ConvertProcessStatus.FAILED);
            textbookRepository.save(textbook);

            log.error("비동기 교안 생성 요청 실패. JobId: {}", jobId, e);
            throw new RuntimeException("교안 생성 요청에 실패했습니다.", e);
        }
    }

    public DocumentProcessingStatusDto getProcessingStatus(String jobId) {
        Optional<Document> documentOpt = documentRepository.findByJobId(jobId);

        if (documentOpt.isEmpty()) {
            throw new IllegalArgumentException("해당 JobId를 찾을 수 없습니다: " + jobId);
        }

        Document document = documentOpt.get();

        return DocumentProcessingStatusDto.builder()
                .jobId(document.getJobId())
                .fileName(document.getOriginalFilename())
                .status(document.getProcessingStatus())
                .progress(document.getProgress())
                .errorMessage(document.getErrorMessage())
                .createdAt(document.getUploadedAt())
                .completedAt(document.getCompletedAt())
                .build();
    }

    private String generateJobId() {
        return UUID.randomUUID().toString();
    }

    private String extractTitle(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "Unknown Document";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(0, lastDotIndex) : filename;
    }
}
