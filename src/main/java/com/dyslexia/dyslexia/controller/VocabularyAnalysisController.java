package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.domain.pdf.VocabularyAnalysis;
import com.dyslexia.dyslexia.dto.CommonResponse;
import com.dyslexia.dyslexia.dto.VocabularyAnalysisSearchRequestDto;
import com.dyslexia.dyslexia.service.VocabularyAnalysisService;
import com.dyslexia.dyslexia.repository.TextbookRepository;
import com.dyslexia.dyslexia.entity.Textbook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("vocabulary-analysis")
@Tag(name = "어휘 분석", description = "교재 내 어휘 분석 결과 조회 API")
public class VocabularyAnalysisController {

  private final VocabularyAnalysisService vocabularyAnalysisService;
  private final TextbookRepository textbookRepository;

  @Operation(
      summary = "어휘 분석 결과 검색",
      description = """
          문서 ID, 페이지 번호, 블록 ID를 기반으로 어휘 분석 결과를 검색합니다.
          페이지 번호와 블록 ID는 선택적 파라미터입니다.
          """
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "검색 성공",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = VocabularyAnalysis.class)
          )
      )
  })
  @PostMapping("/search")
  public ResponseEntity<CommonResponse<List<VocabularyAnalysis>>> searchVocabularyAnalysis(
      @Parameter(description = "검색 조건", required = true)
      @RequestBody VocabularyAnalysisSearchRequestDto request) {
    // Resolve textbookId from documentId if needed
    Long textbookId = request.getTextbookId();
    log.info("=== 어휘 분석 검색 요청 시작 ===");
    log.info("요청 파라미터 - documentId: {}, textbookId: {}, pageNumber: {}, blockId: {}",
        request.getDocumentId(), request.getTextbookId(), request.getPageNumber(), request.getBlockId());

    if (textbookId == null && request.getDocumentId() != null) {
      log.info("documentId({})로 textbookId 조회 중...", request.getDocumentId());
      textbookId = textbookRepository.findByDocumentId(request.getDocumentId())
          .map(tb -> {
            log.info("textbook 발견: id={}, documentId={}", tb.getId(), tb.getDocumentId());
            return tb.getId();
          })
          .orElse(null);

      if (textbookId == null) {
        log.warn("documentId({})에 해당하는 textbook을 찾을 수 없습니다", request.getDocumentId());
      }
    }

    log.info("최종 검색 조건 - textbookId: {}, pageNumber: {}, blockId: {}",
        textbookId, request.getPageNumber(), request.getBlockId());

    List<VocabularyAnalysis> results = vocabularyAnalysisService.searchVocabularyAnalysis(
        textbookId,
        request.getDocumentId(),
        request.getPageNumber(),
        request.getBlockId());

    log.info("검색 결과: {}개 항목 발견", results != null ? results.size() : 0);
    if (results != null && !results.isEmpty()) {
      log.debug("첫 번째 결과 예시: word={}, blockId={}, textbookId={}",
        results.get(0).getWord(), results.get(0).getBlockId(), results.get(0).getTextbookId());
    } else if (textbookId != null) {
      // textbookId가 있는데도 결과가 없다면 실제 데이터 확인
      log.warn("textbookId={}로 검색했지만 결과가 없습니다. DB에 해당 데이터가 있는지 확인 필요", textbookId);
    }

    // S3 기반 온디맨드 생성은 사용하지 않습니다. FastAPI 콜백(인라인)으로 DB에 저장됩니다.

    return ResponseEntity.ok(new CommonResponse<>("어휘 분석 검색 완료", results));
  }

  @GetMapping("/debug/textbook/{documentId}")
  @Operation(summary = "디버그: DocumentId로 Textbook 조회", description = "DocumentId로 Textbook을 찾을 수 있는지 확인")
  public ResponseEntity<?> debugTextbookLookup(@PathVariable Long documentId) {
    log.info("=== 디버그: DocumentId({}) → Textbook 조회 ===", documentId);

    java.util.Optional<Textbook> textbookOpt = textbookRepository.findByDocumentId(documentId);

    if (textbookOpt.isPresent()) {
      Textbook textbook = textbookOpt.get();
      log.info("✅ Textbook 발견: id={}, documentId={}, title={}",
        textbook.getId(), textbook.getDocumentId(), textbook.getTitle());

      return ResponseEntity.ok(java.util.Map.of(
        "success", true,
        "textbook", java.util.Map.of(
          "id", textbook.getId(),
          "documentId", textbook.getDocumentId(),
          "title", textbook.getTitle(),
          "pageCount", textbook.getPageCount()
        )
      ));
    } else {
      log.warn("❌ DocumentId({})에 해당하는 Textbook을 찾을 수 없음", documentId);

      return ResponseEntity.ok(java.util.Map.of(
        "success", false,
        "message", "DocumentId " + documentId + "에 해당하는 Textbook을 찾을 수 없습니다"
      ));
    }
  }
}
