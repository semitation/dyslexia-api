package com.dyslexia.dyslexia.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.dyslexia.dyslexia.domain.pdf.VocabularyAnalysis;
import com.dyslexia.dyslexia.domain.pdf.VocabularyAnalysisRepository;
import com.dyslexia.dyslexia.dto.BlockVocabularyResultDto;
import com.dyslexia.dyslexia.dto.VocabularyCompleteRequestDto;
import com.dyslexia.dyslexia.dto.VocabularyItemDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VocabularyCallbackService {

    private final VocabularyAnalysisRepository vocabularyAnalysisRepository;
    private final ObjectMapper objectMapper;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String defaultBucketName;

    @Transactional
    public int handleCompletion(VocabularyCompleteRequestDto request) {
        int savedCount = 0;

        if (request.getBlocks() != null && !request.getBlocks().isEmpty()) {
            savedCount += processInlineBlocks(request);
        } else if (request.getS3BlocksPrefix() != null && !request.getS3BlocksPrefix().isBlank()) {
            savedCount += processS3Blocks(request);
        } else {
            log.warn("어휘 분석 콜백에 처리할 블록 정보가 없습니다. jobId={}, textbookId={}", request.getJobId(), request.getTextbookId());
        }

        log.info("어휘 분석 콜백 처리 완료: jobId={}, textbookId={}, savedItems={}", request.getJobId(), request.getTextbookId(), savedCount);
        return savedCount;
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

    private int processS3Blocks(VocabularyCompleteRequestDto request) {
        S3Location loc = parseS3Location(request.getS3BlocksPrefix());
        String bucket = loc.bucket() != null ? loc.bucket() : defaultBucketName;
        String prefix = ensureTrailingSlash(loc.key());

        int saved = 0;
        String continuation = null;
        do {
            ListObjectsV2Request listReq = new ListObjectsV2Request()
                .withBucketName(bucket)
                .withPrefix(prefix)
                .withContinuationToken(continuation);
            ListObjectsV2Result listRes = amazonS3.listObjectsV2(listReq);

            List<VocabularyAnalysis> batch = new ArrayList<>();
            for (S3ObjectSummary summary : listRes.getObjectSummaries()) {
                String key = summary.getKey();
                if (!key.endsWith(".json")) continue;
                if (key.endsWith("summary.json")) continue; // skip summary here
                try (S3Object s3Object = amazonS3.getObject(bucket, key);
                     InputStream in = s3Object.getObjectContent()) {
                    JsonNode node = objectMapper.readTree(in);
                    BlockVocabularyResultDto block = objectMapper.convertValue(node, BlockVocabularyResultDto.class);
                    Long textbookId = block.getTextbookId() != null ? block.getTextbookId() : request.getTextbookId();
                    if (block.getVocabularyItems() == null) continue;
                    for (VocabularyItemDto item : block.getVocabularyItems()) {
                        batch.add(toEntity(textbookId, block.getPageNumber(), block.getBlockId(), item, block.getCreatedAt()));
                    }
                } catch (Exception e) {
                    log.error("S3 블록 파싱 실패: s3://{}/{} (jobId={})", bucket, key, request.getJobId(), e);
                }
            }
            saved += upsertAll(batch);
            continuation = listRes.isTruncated() ? listRes.getNextContinuationToken() : null;
        } while (continuation != null);

        return saved;
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
            if (item.getPhonemeAnalysis() != null) {
                phonemeJson = objectMapper.writeValueAsString(item.getPhonemeAnalysis());
            }
        } catch (Exception e) {
            log.warn("phonemeAnalysis 직렬화 실패 - 생략. blockId={}, word={}", blockId, item.getWord());
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

    private static String ensureTrailingSlash(String s) {
        if (s == null || s.isBlank()) return s;
        return s.endsWith("/") ? s : s + "/";
    }

    private S3Location parseS3Location(String s3UrlOrPrefix) {
        if (s3UrlOrPrefix == null) return new S3Location(defaultBucketName, "");
        String url = s3UrlOrPrefix.trim();
        if (url.startsWith("s3://")) {
            String rest = url.substring(5);
            int slash = rest.indexOf('/')
                ;
            if (slash < 0) {
                return new S3Location(rest, "");
            }
            String bucket = rest.substring(0, slash);
            String key = rest.substring(slash + 1);
            return new S3Location(bucket, key);
        }
        // http(s) style: try to extract path as key; bucket unknown -> use default
        try {
            java.net.URL u = new java.net.URL(url);
            String path = u.getPath();
            if (path.startsWith("/")) path = path.substring(1);
            return new S3Location(null, path);
        } catch (Exception e) {
            // treat as plain key
            return new S3Location(null, url);
        }
    }

    private record S3Location(String bucket, String key) {}
}

