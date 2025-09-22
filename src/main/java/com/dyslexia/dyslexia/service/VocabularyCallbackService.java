package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.domain.pdf.VocabularyAnalysis;
import com.dyslexia.dyslexia.domain.pdf.VocabularyAnalysisRepository;
import com.dyslexia.dyslexia.dto.BlockVocabularyResultDto;
import com.dyslexia.dyslexia.dto.VocabularyBlockRequestDto;
import com.dyslexia.dyslexia.dto.VocabularyCompleteRequestDto;
import com.dyslexia.dyslexia.dto.VocabularyItemDto;
import com.dyslexia.dyslexia.entity.Document;
import com.dyslexia.dyslexia.entity.Textbook;
import com.dyslexia.dyslexia.repository.DocumentRepository;
import com.dyslexia.dyslexia.repository.TextbookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VocabularyCallbackService {

    private final VocabularyAnalysisRepository vocabularyAnalysisRepository;
    private final ObjectMapper objectMapper;
    private final DocumentRepository documentRepository;
    private final TextbookRepository textbookRepository;

    @Transactional
    public int handleBlockCallback(VocabularyBlockRequestDto request) {
        log.info("=== 블록 콜백 처리 시작 ===");
        log.info("jobId: {}", request.getJobId());
        log.info("textbookId: {}", request.getTextbookId());
        log.info("blockId: {}", request.getBlockId());
        log.info("pageNumber: {}", request.getPageNumber());
        log.info("originalSentence: {}", request.getOriginalSentence());
        log.info("vocabularyItems 수: {}", request.getVocabularyItems() != null ? request.getVocabularyItems().size() : 0);

        // 음운 분석 정보 확인
        if (request.getVocabularyItems() != null) {
            long phonemeCount = request.getVocabularyItems().stream()
                .mapToLong(item -> {
                    boolean hasPhonemeJson = item.getPhonemeAnalysisJson() != null && !item.getPhonemeAnalysisJson().isBlank();
                    boolean hasPhonemeObj = item.getPhonemeAnalysis() != null;
                    if (hasPhonemeJson || hasPhonemeObj) {
                        log.debug("음운 분석 포함 - word: {}, hasJson: {}, hasObj: {}", item.getWord(), hasPhonemeJson, hasPhonemeObj);
                        return 1;
                    }
                    return 0;
                })
                .sum();
            log.info("음운 분석 포함 항목 수: {}/{}", phonemeCount, request.getVocabularyItems().size());
        }

        // 어휘 항목 검증 및 보정
        List<VocabularyItemDto> processedItems = processVocabularyItems(request.getVocabularyItems(), request.getOriginalSentence());

        // Entity 변환
        List<VocabularyAnalysis> entities = new ArrayList<>();
        for (VocabularyItemDto item : processedItems) {
            VocabularyAnalysis entity = toEntity(
                request.getTextbookId(),
                request.getPageNumber(),
                request.getBlockId(),
                item,
                request.getCreatedAt()
            );
            entities.add(entity);

            // 생성된 엔티티의 음운 분석 정보 로그
            if (entity.getPhonemeAnalysisJson() != null && !entity.getPhonemeAnalysisJson().isBlank()) {
                log.debug("DB 저장 예정 - word: {}, phonemeJson length: {}", entity.getWord(), entity.getPhonemeAnalysisJson().length());
            }
        }

        // DB 저장 (upsert)
        int savedCount = upsertAll(entities);

        log.info("=== 블록 콜백 처리 완료 ===");
        log.info("처리 결과 - jobId: {}, blockId: {}, processed: {}, saved: {}",
            request.getJobId(), request.getBlockId(), processedItems.size(), savedCount);

        return savedCount;
    }

    @Transactional
    public int handleCompletion(VocabularyCompleteRequestDto request) {
        int savedCount = 0;

        // Resolve textbookId if missing or invalid (<= 0)
        if (request.getTextbookId() == null || (request.getTextbookId() != null && request.getTextbookId() <= 0)) {
            resolveTextbookIdFromJobId(request);
        }

        if (request.getBlocks() != null && !request.getBlocks().isEmpty()) {
            log.info("어휘 콜백 처리 모드: inline blocks (count={})", request.getBlocks().size());
            savedCount += processInlineBlocks(request);
        } else {
            log.warn("어휘 분석 콜백에 처리할 블록 정보가 없습니다. (S3 포인터는 지원하지 않음) jobId={}, textbookId={}", request.getJobId(), request.getTextbookId());
        }

        log.info("어휘 분석 콜백 처리 완료: jobId={}, textbookId={}, savedItems={}", request.getJobId(), request.getTextbookId(), savedCount);
        return savedCount;
    }

    private void resolveTextbookIdFromJobId(VocabularyCompleteRequestDto request) {
        try {
            if (request.getJobId() == null || request.getJobId().isBlank()) return;
            documentRepository.findByJobId(request.getJobId()).ifPresent(document -> {
                Long textbookId = findTextbookIdByDocument(document);
                if (textbookId != null && textbookId > 0) {
                    request.setTextbookId(textbookId);
                    log.info("어휘 콜백 textbookId 자동 해석: jobId={} -> textbookId={}", request.getJobId(), textbookId);
                } else {
                    log.warn("jobId={}로 textbookId를 찾지 못했습니다. (문서는 존재)", request.getJobId());
                }
            });
        } catch (Exception e) {
            log.warn("jobId로부터 textbookId 해석 중 오류: jobId={}", request.getJobId(), e);
        }
    }

    private Long findTextbookIdByDocument(Document document) {
        // If a textbook is already created for this document, reuse it
        return textbookRepository.findByDocumentId(document.getId())
            .map(Textbook::getId)
            .orElse(null);
    }

    private int processInlineBlocks(VocabularyCompleteRequestDto request) {
        List<BlockVocabularyResultDto> blocks = request.getBlocks();
        List<VocabularyAnalysis> entities = new ArrayList<>();
        for (BlockVocabularyResultDto block : blocks) {
            if (block.getVocabularyItems() == null) continue;
            Long textbookId = block.getTextbookId() != null ? block.getTextbookId() : request.getTextbookId();
            for (VocabularyItemDto item : block.getVocabularyItems()) {
                entities.add(toEntity(textbookId, block.getPageNumber(), block.getBlockId(), item, block.getCreatedAt()));
            }
        }
        return upsertAll(entities);
    }


    private VocabularyAnalysis toEntity(Long textbookId, Integer pageNumber, String blockId, VocabularyItemDto item, String createdAtStr) {
        String examplesText = null;
        try {
            if (item.getExamples() != null) {
                if (item.getExamples() instanceof String) {
                    examplesText = (String) item.getExamples();
                } else {
                    examplesText = objectMapper.writeValueAsString(item.getExamples());
                }
            }
        } catch (Exception e) {
            log.warn("examples 직렬화 실패 - 문자열로 저장하지 않음. blockId={}, word={}", blockId, item.getWord());
        }

        String phonemeJson = null;
        try {
            // 우선순위: phonemeAnalysisJson (문자열) -> phonemeAnalysis (객체) -> null
            if (item.getPhonemeAnalysisJson() != null && !item.getPhonemeAnalysisJson().isBlank()) {
                phonemeJson = item.getPhonemeAnalysisJson();
                log.debug("phonemeAnalysisJson 문자열 사용: blockId={}, word={}", blockId, item.getWord());
            } else if (item.getPhonemeAnalysis() != null) {
                phonemeJson = objectMapper.writeValueAsString(item.getPhonemeAnalysis());
                log.debug("phonemeAnalysis 객체 직렬화: blockId={}, word={}", blockId, item.getWord());
            }
        } catch (Exception e) {
            log.warn("phonemeAnalysis 직렬화 실패 - 생략. blockId={}, word={}, error={}", blockId, item.getWord(), e.getMessage());
        }

        LocalDateTime createdAt = parseDateTime(createdAtStr);

        return VocabularyAnalysis.builder()
            .textbookId(textbookId)
            .pageNumber(pageNumber)
            .blockId(blockId)
            .word(item.getWord())
            .startIndex(item.getStartIndex())
            .endIndex(item.getEndIndex())
            .definition(trim255(item.getDefinition()))
            .simplifiedDefinition(trim255(item.getSimplifiedDefinition()))
            .examples(examplesText)
            .difficultyLevel(item.getDifficultyLevel())
            .reason(trim255(item.getReason()))
            .gradeLevel(item.getGradeLevel())
            .phonemeAnalysisJson(phonemeJson)
            .createdAt(createdAt != null ? createdAt : LocalDateTime.now())
            .build();
    }

    private int upsertAll(List<VocabularyAnalysis> entities) {
        int[] counter = {0};
        for (VocabularyAnalysis e : entities) {
            try {
                Optional<VocabularyAnalysis> existing = vocabularyAnalysisRepository
                    .findByTextbookIdAndBlockIdAndWordAndStartIndexAndEndIndex(
                        e.getTextbookId(), e.getBlockId(), e.getWord(), e.getStartIndex(), e.getEndIndex());

                if (existing.isPresent()) {
                    VocabularyAnalysis x = existing.get();
                    x.setDefinition(e.getDefinition());
                    x.setSimplifiedDefinition(e.getSimplifiedDefinition());
                    x.setReason(e.getReason());
                    x.setExamples(e.getExamples());
                    x.setDifficultyLevel(e.getDifficultyLevel());
                    x.setGradeLevel(e.getGradeLevel());
                    x.setPhonemeAnalysisJson(e.getPhonemeAnalysisJson());
                    vocabularyAnalysisRepository.save(x);
                } else {
                    vocabularyAnalysisRepository.save(e);
                }
                counter[0]++;
            } catch (Exception ex) {
                log.error("어휘 항목 저장 실패: textbookId={}, blockId={}, word={}", e.getTextbookId(), e.getBlockId(), e.getWord(), ex);
            }
        }
        return counter[0];
    }

    private static String trim255(String s) {
        if (s == null) return null;
        return s.length() <= 255 ? s : s.substring(0, 255);
    }

    private static LocalDateTime parseDateTime(String iso) {
        if (iso == null || iso.isBlank()) return null;
        try {
            return OffsetDateTime.parse(iso).toLocalDateTime();
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(iso);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private List<VocabularyItemDto> processVocabularyItems(List<VocabularyItemDto> items, String originalSentence) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        // 최대 5개만 처리 (상위 5개)
        List<VocabularyItemDto> processedItems = items.stream()
            .limit(5)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // 각 항목별 검증 및 보정
        for (VocabularyItemDto item : processedItems) {
            // 인덱스 유효성 검사 및 보정
            if (item.getEndIndex() != null && item.getStartIndex() != null && item.getEndIndex() <= item.getStartIndex()) {
                log.warn("잘못된 인덱스: word={}, start={}, end={} - 보정 시도", item.getWord(), item.getStartIndex(), item.getEndIndex());
                fixIndexes(item, originalSentence);
            }

            // 텍스트 길이 제한
            item.setDefinition(trim255(item.getDefinition()));
            item.setSimplifiedDefinition(trim255(item.getSimplifiedDefinition()));
            item.setReason(trim255(item.getReason()));
        }

        return processedItems;
    }

    private void fixIndexes(VocabularyItemDto item, String originalSentence) {
        if (originalSentence == null || item.getWord() == null) {
            log.warn("원본 문장 또는 단어가 없어 인덱스 보정 불가: word={}", item.getWord());
            return;
        }

        // 문장에서 단어의 첫 번째 등장 위치 찾기
        int foundIndex = originalSentence.indexOf(item.getWord());
        if (foundIndex >= 0) {
            item.setStartIndex(foundIndex);
            item.setEndIndex(foundIndex + item.getWord().length());
            log.info("인덱스 보정 완료: word={}, start={}, end={}", item.getWord(), item.getStartIndex(), item.getEndIndex());
        } else {
            log.warn("문장에서 단어를 찾을 수 없어 인덱스 보정 실패: word={}, sentence={}", item.getWord(), originalSentence);
        }
    }


}
