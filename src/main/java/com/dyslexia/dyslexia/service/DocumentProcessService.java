package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.entity.Document;
import com.dyslexia.dyslexia.entity.Page;
import com.dyslexia.dyslexia.entity.PageImage;
import com.dyslexia.dyslexia.entity.PageTip;
import com.dyslexia.dyslexia.entity.Teacher;
import com.dyslexia.dyslexia.enums.DocumentProcessStatus;
import com.dyslexia.dyslexia.enums.Grade;
import com.dyslexia.dyslexia.repository.DocumentRepository;
import com.dyslexia.dyslexia.repository.PageImageRepository;
import com.dyslexia.dyslexia.repository.PageRepository;
import com.dyslexia.dyslexia.repository.PageTipRepository;
import com.dyslexia.dyslexia.repository.TeacherRepository;
import com.dyslexia.dyslexia.util.DocumentProcessHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import com.dyslexia.dyslexia.domain.pdf.VocabularyAnalysis;
import com.dyslexia.dyslexia.domain.pdf.VocabularyAnalysisRepository;
import com.dyslexia.dyslexia.domain.pdf.TextBlock;
import com.dyslexia.dyslexia.service.VocabularyAnalysisPromptService;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentProcessService {

    private final DocumentRepository documentRepository;
    private final PageRepository pageRepository;
    private final PageTipRepository pageTipRepository;
    private final PageImageRepository pageImageRepository;
    private final TeacherRepository teacherRepository;
    private final PdfParserService pdfParserService;
    private final AIPromptService aiPromptService;
    private final StorageService storageService;
    private final Executor taskExecutor;
    private final ObjectMapper objectMapper;
    private final DeepLTranslatorService deepLTranslatorService;
    private final VocabularyAnalysisRepository vocabularyAnalysisRepository;
    private final VocabularyAnalysisPromptService vocabularyAnalysisPromptService;

    @Transactional
    public Document uploadDocument(Long teacherId, MultipartFile file, String title, Grade grade, String subjectPath) throws IOException {
        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new IllegalArgumentException("선생님을 찾을 수 없습니다."));

        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        log.info("파일 업로드 요청 처리 - 원본 파일명: {}, 고유 파일명: {}, 교사ID: {}", 
                originalFilename, uniqueFilename, teacherId);
                
        String filePath = storageService.store(file, uniqueFilename, teacherId);
        log.info("파일 저장 경로: {}", filePath);

        // 파일 경로에서 폴더 경로 추출 (파일명 제외)
        String folderPath = filePath.substring(0, filePath.lastIndexOf("/"));
        log.info("PDF 폴더 경로: {}", folderPath);

        Document document = Document.builder()
            .teacher(teacher)
            .title(title)
            .originalFilename(originalFilename)
            .filePath(filePath)
            .fileSize(file.getSize())
            .mimeType(file.getContentType())
            .grade(grade)
            .subjectPath(subjectPath)
            .processStatus(DocumentProcessStatus.PENDING)
            .build();

        document = documentRepository.save(document);

        final Long documentId = document.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                CompletableFuture.runAsync(() -> processDocumentAsync(documentId), taskExecutor);
            }
        });

        return document;
    }

    private void processDocumentAsync(Long documentId) {
        try {
            log.info("문서 ID: {}의 비동기 처리를 시작합니다.", documentId);

            Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다."));

            document.setProcessStatus(DocumentProcessStatus.PROCESSING);
            documentRepository.save(document);

            List<String> rawPages = pdfParserService.parsePages(document.getFilePath());
            document.setPageCount(rawPages.size());
            documentRepository.save(document);

            log.info("문서 ID: {}의 전체 페이지 수: {}", documentId, rawPages.size());

            for (int i = 0; i < rawPages.size(); i++) {
                int pageNumber = i + 1;
                log.info("문서 ID: {}, 페이지 번호: {} 처리 시작", documentId, pageNumber);
                processPage(document, pageNumber, rawPages.get(i));
                log.info("문서 ID: {}, 페이지 번호: {} 처리 완료", documentId, pageNumber);
            }

            document.setProcessStatus(DocumentProcessStatus.COMPLETED);
            documentRepository.save(document);
            log.info("문서 ID: {}의 모든 처리가 완료되었습니다.", documentId);

        } catch (Exception e) {
            log.error("문서 처리 중 오류 발생: 문서 ID: {}", documentId, e);
            Document document = documentRepository.findById(documentId).orElse(null);
            if (document != null) {
                document.setProcessStatus(DocumentProcessStatus.FAILED);
                documentRepository.save(document);
                log.error("문서 ID: {}의 상태를 FAILED로 변경했습니다.", documentId);
            }
        }
    }

    @Transactional
    public void processPage(Document document, int pageNumber, String rawContent) {
        try {
            log.info("페이지 처리 시작: 문서 ID: {}, 페이지 번호: {}", document.getId(), pageNumber);
            
            // 파일 경로에서 폴더 경로 추출 (파일명 제외)
            String filePath = document.getFilePath();
            String folderPath = filePath.substring(0, filePath.lastIndexOf("/"));
            
            // ThreadLocal에 문서 정보 설정
            DocumentProcessHolder.setDocumentId(document.getId());
            DocumentProcessHolder.setPdfName(document.getOriginalFilename());
            DocumentProcessHolder.setTeacherId(document.getTeacher().getId().toString());
            DocumentProcessHolder.setPdfFolderPath(folderPath);
            
            try {
                Optional<Page> existingPage = pageRepository.findByDocumentAndPageNumber(document, pageNumber);
                if (existingPage.isPresent() &&
                    existingPage.get().getProcessingStatus() == DocumentProcessStatus.COMPLETED) {
                    log.info("페이지가 이미 처리되었습니다: 문서 ID: {}, 페이지 번호: {}", document.getId(), pageNumber);
                    return;
                }

                // 1. OpenAI 번역 수행
                String translatedContent = aiPromptService.translateTextWithOpenAI(rawContent);

                // 2. 번역된 텍스트로 AI Block 처리
                AIPromptService.PageBlockAnalysisResult blockAnalysisResult = aiPromptService.processPageContent(translatedContent, document.getGrade());
                String processedContentStr = blockAnalysisResult.getOriginalContent();
                com.fasterxml.jackson.databind.JsonNode processedContent;
                try {
                    processedContent = objectMapper.readTree(processedContentStr);
                } catch (Exception e) {
                    log.error("JSON 변환 중 오류 발생", e);
                    throw new RuntimeException("처리된 콘텐츠를 JSON으로 변환하는 중 오류가 발생했습니다.", e);
                }

                log.info("블럭 개수: {}", blockAnalysisResult.getBlocks().size());
                log.info("블럭 한개: {}", blockAnalysisResult.getBlocks().get(0));
                List<TextBlock> textBlocks = blockAnalysisResult.getBlocks().stream()
                    .filter(block -> block.getType() != null && block.getType().name().equals("TEXT"))
                    .map(block -> (TextBlock) block)
                    .toList();

                log.info("textBlock 개수: {}", textBlocks.size());

                textBlocks.parallelStream()
                    .forEach(textBlock -> analyzeAndSaveVocabularyAsync(textBlock, document.getId(), pageNumber));

                // 3. 메타데이터 추출 (번역된 텍스트 기반)
                String sectionTitle = aiPromptService.extractSectionTitle(translatedContent);
                Integer readingLevel = aiPromptService.calculateReadingLevel(translatedContent);
                Integer wordCount = aiPromptService.countWords(translatedContent);
                Float complexityScore = aiPromptService.calculateComplexityScore(translatedContent);

                Page page;
                if (existingPage.isPresent()) {
                    page = existingPage.get();
                    page.setProcessingStatus(DocumentProcessStatus.PROCESSING);
                    pageRepository.save(page);
                } else {
                    page = Page.builder()
                        .document(document)
                        .pageNumber(pageNumber)
                        .originalContent(rawContent)
                        .processedContent(processedContent)
                        .sectionTitle(sectionTitle)
                        .readingLevel(readingLevel)
                        .wordCount(wordCount)
                        .complexityScore(complexityScore)
                        .processingStatus(DocumentProcessStatus.PROCESSING)
                        .build();

                    page = pageRepository.save(page);
                }

                if (existingPage.isPresent()) {
                    pageTipRepository.deleteAll(pageTipRepository.findByPageId(page.getId()));
                    pageImageRepository.deleteAll(pageImageRepository.findByPageId(page.getId()));
                }

                log.info("용어 추출 시작: 문서 ID: {}, 페이지 번호: {}", document.getId(), pageNumber);
                List<AIPromptService.TermInfo> terms = aiPromptService.extractTerms(translatedContent, document.getGrade());
                for (AIPromptService.TermInfo termInfo : terms) {
                    PageTip pageTip = PageTip.builder()
                        .page(page)
                        .term(termInfo.getTerm())
                        .simplifiedExplanation(termInfo.getExplanation())
                        .termPosition(termInfo.getPositionJson())
                        .termType(termInfo.getTermType())
                        .visualAidNeeded(termInfo.isVisualAidNeeded())
                        .readAloudText(termInfo.getReadAloudText())
                        .build();

                    pageTipRepository.save(pageTip);
                }
                log.info("용어 {} 개 처리 완료: 문서 ID: {}, 페이지 번호: {}", terms.size(), document.getId(), pageNumber);

                log.info("이미지 생성 시작: 문서 ID: {}, 페이지 번호: {}", document.getId(), pageNumber);
                List<AIPromptService.ImageInfo> images = aiPromptService.extractOrGenerateImages(translatedContent, terms);
                for (AIPromptService.ImageInfo imageInfo : images) {
                    PageImage pageImage = PageImage.builder()
                        .page(page)
                        .imageUrl(imageInfo.getImageUrl())
                        .imageType(imageInfo.getImageType())
                        .conceptReference(imageInfo.getConceptReference())
                        .altText(imageInfo.getAltText())
                        .positionInPage(imageInfo.getPositionJson())
                        .build();

                    pageImageRepository.save(pageImage);
                }
                log.info("이미지 {} 개 처리 완료: 문서 ID: {}, 페이지 번호: {}", images.size(), document.getId(), pageNumber);

                page.setProcessingStatus(DocumentProcessStatus.COMPLETED);
                pageRepository.save(page);

                log.info("페이지 처리 완료: 문서 ID: {}, 페이지 번호: {}", document.getId(), pageNumber);
            } finally {
                // ThreadLocal 정리
                DocumentProcessHolder.clear();
            }

        } catch (Exception e) {
            log.error("페이지 처리 중 오류 발생: 문서 ID: {}, 페이지 번호: {}", document.getId(), pageNumber, e);

            Optional<Page> page = pageRepository.findByDocumentAndPageNumber(document, pageNumber);
            if (page.isPresent()) {
                page.get().setProcessingStatus(DocumentProcessStatus.FAILED);
                pageRepository.save(page.get());
                log.error("페이지 ID: {}의 상태를 FAILED로 변경했습니다.", page.get().getId());
            }
            
            // 오류 발생 시에도 ThreadLocal 반드시 정리
            DocumentProcessHolder.clear();
        }
    }

    @Transactional(readOnly = true)
    public DocumentProcessStatus getDocumentProcessStatus(Long documentId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다."));
        return document.getProcessStatus();
    }

    @Transactional(readOnly = true)
    public int calculateDocumentProcessProgress(Long documentId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다."));

        if (document.getProcessStatus() == DocumentProcessStatus.COMPLETED) {
            return 100;
        } else if (document.getProcessStatus() == DocumentProcessStatus.PENDING) {
            return 0;
        } else if (document.getProcessStatus() == DocumentProcessStatus.FAILED) {
            // 실패한 경우, 처리된 페이지 비율 계산
            long completedPages = pageRepository.findByDocumentId(documentId).stream()
                .filter(p -> p.getProcessingStatus() == DocumentProcessStatus.COMPLETED)
                .count();

            int totalPages = document.getPageCount() != null ? document.getPageCount() : 0;
            if (totalPages > 0) {
                return (int) ((completedPages * 100) / totalPages);
            }
            return 0;
        } else {
            // PROCESSING 상태인 경우
            long completedPages = pageRepository.findByDocumentId(documentId).stream()
                .filter(p -> p.getProcessingStatus() == DocumentProcessStatus.COMPLETED)
                .count();

            int totalPages = document.getPageCount() != null ? document.getPageCount() : 0;
            if (totalPages > 0) {
                return (int) ((completedPages * 100) / totalPages);
            }
            return 0;
        }
    }

    @Transactional
    public Document retryDocumentProcessing(Long documentId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다."));

        if (document.getProcessStatus() != DocumentProcessStatus.FAILED) {
            throw new IllegalStateException("실패한 문서만 재처리할 수 있습니다.");
        }

        document.setProcessStatus(DocumentProcessStatus.PENDING);
        document = documentRepository.save(document);

        CompletableFuture.runAsync(() -> processDocumentAsync(documentId), taskExecutor);

        return document;
    }

    // Block의 content에 대해 어휘 분석을 실행하고, 결과를 VocabularyAnalysis 엔티티로 저장 (비동기)
    private void analyzeAndSaveVocabularyAsync(TextBlock textBlock, Long documentId, int pageNumber) {
        CompletableFuture.runAsync(() -> {
            try {
                // 1. 기본 어휘 분석 수행
                String basicAnalysisJson = vocabularyAnalysisPromptService.analyzeVocabularyBasic(textBlock.getText(), 3);
                List<Map<String, Object>> basicAnalysisList = objectMapper.readValue(basicAnalysisJson, List.class);
                
                // 2. 각 단어에 대해 음소 분석 수행 및 저장
                for (Map<String, Object> basicAnalysis : basicAnalysisList) {
                    String word = (String) basicAnalysis.get("word");
                    String phonemeAnalysisJson = vocabularyAnalysisPromptService.analyzePhonemeAnalysis(word);
                    
                    // 3. 기본 분석과 음소 분석 결과를 결합하여 저장
                    VocabularyAnalysis entity = VocabularyAnalysis.builder()
                        .documentId(documentId)
                        .pageNumber(pageNumber)
                        .blockId(textBlock.getId())
                        .word(word)
                        .startIndex((Integer) basicAnalysis.getOrDefault("startIndex", null))
                        .endIndex((Integer) basicAnalysis.getOrDefault("endIndex", null))
                        .definition((String) basicAnalysis.getOrDefault("definition", null))
                        .simplifiedDefinition((String) basicAnalysis.getOrDefault("simplifiedDefinition", null))
                        .examples(basicAnalysis.get("examples") != null ? objectMapper.writeValueAsString(basicAnalysis.get("examples")) : null)
                        .difficultyLevel((String) basicAnalysis.getOrDefault("difficultyLevel", null))
                        .reason((String) basicAnalysis.getOrDefault("reason", null))
                        .gradeLevel(basicAnalysis.get("gradeLevel") != null ? (Integer) basicAnalysis.get("gradeLevel") : null)
                        .phonemeAnalysisJson(phonemeAnalysisJson)
                        .createdAt(java.time.LocalDateTime.now())
                        .build();
                    vocabularyAnalysisRepository.save(entity);
                    log.info("어휘 분석 저장 완료: word={}, documentId={}, pageNumber={}", word, documentId, pageNumber);
                }
            } catch (Exception e) {
                log.error("어휘 분석 및 저장 실패: blockId={}, pageNumber={}", textBlock.getId(), pageNumber, e);
            }
        }, taskExecutor);
    }

} 