package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.PageContentResponseDto;
import com.dyslexia.dyslexia.dto.PageTipResponseDto;
import com.dyslexia.dyslexia.dto.CommonResponse;
import com.dyslexia.dyslexia.service.TextbookContentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("textbooks")
@RequiredArgsConstructor
@Tag(name = "Textbook Content API", description = "교재 컨텐츠 관련 API")
public class TextbookContentController {

  private final TextbookContentService textbookContentService;
  private final ObjectMapper objectMapper;

  @GetMapping("/pages")
  @Operation(
      summary = "교재 페이지 조회",
      description = """
          교재 ID 기반으로 생성된 콘텐츠 JSON(page)를 조회합니다. 페이지 번호를 지정하면 특정 페이지만 조회합니다.
          
          Block 구조 예시:
          [
            {"id":"1","type":"HEADING1","text":"챕터 제목"},
            {"id":"2","type":"TEXT","text":"본문"},
            {"id":"3","type":"LIST","items":["항목1","항목2"]}
          ]
          """,
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "페이지 목록 조회 성공",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(
                      schema = @Schema(implementation = PageContentResponseDto.class)
                  )
              )
          )
      }
  )

  public ResponseEntity<CommonResponse<List<PageContentResponseDto>>> findPageByTextbook(
      @Parameter(description = "문서 ID", required = true) @RequestParam(name = "textbookId") Long textbookId,
      @Parameter(description = "페이지 번호 (선택사항)") @RequestParam(name = "page", required = false) Integer pageNumber) {

    List<PageContentResponseDto> responses = textbookContentService
        .getPagesByTextbookId(textbookId, pageNumber).stream()
        .map(page -> PageContentResponseDto.fromEntity(page, objectMapper))
        .toList();

    return ResponseEntity.ok(new CommonResponse<>("페이지 팁 조회 성공", responses));
  }

  @GetMapping("/pages/{pageId}/tips")
  @Operation(
      summary = "페이지 팁 조회",
      description = "페이지 ID 기반으로 해당 페이지의 팁을 조회합니다.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "페이지 팁 목록 조회 성공",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(
                      schema = @Schema(implementation = PageTipResponseDto.class)
                  )
              )
          )
      }
  )
  public ResponseEntity<CommonResponse<List<PageTipResponseDto>>> findPageTipsByPage(
      @Parameter(description = "페이지 ID", required = true) @PathVariable("pageId") Long pageId) {

    List<PageTipResponseDto> responses = textbookContentService.getPageTipsByPageId(pageId).stream()
        .map(PageTipResponseDto::fromEntity)
        .toList();

    return ResponseEntity.ok(new CommonResponse<>("페이지 팁 조회 성공", responses));
  }
}