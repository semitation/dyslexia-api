package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.PageDetailResponseDto;
import com.dyslexia.dyslexia.dto.PageDto;
import com.dyslexia.dyslexia.dto.PageListResponseDto;
import com.dyslexia.dyslexia.dto.PageProgressUpdateRequestDto;
import com.dyslexia.dyslexia.dto.TextbookDto;
import com.dyslexia.dyslexia.exception.GlobalApiResponse;
import com.dyslexia.dyslexia.service.StudentTextbookService;
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

@Tag(name = "StudentTextbook", description = "학생 교재 관리 API")
@RestController
@RequestMapping("students/{studentId}/textbooks")
@RequiredArgsConstructor
@Slf4j
public class StudentTextbookController {

  private final StudentTextbookService studentTextbookService;

  @Operation(summary = "학생에게 할당된 교재 목록 조회", description = "학생에게 할당된 모든 교재 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = TextbookDto.class))),
      @ApiResponse(responseCode = "404", description = "학생을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping
  public ResponseEntity<GlobalApiResponse<List<TextbookDto>>> findAllAssignedTextbookByStudent(
      @Parameter(description = "학생 ID", required = true)
      @PathVariable Long studentId) {

    log.info("학생({})의 할당된 교재 목록 조회 요청", studentId);

    var textbooks = studentTextbookService.getAssignedTextbooks(studentId);

    String message = textbooks.isEmpty() ? "할당된 교재가 없습니다." : "할당된 교재 목록 조회 성공";

    return ResponseEntity.ok(GlobalApiResponse.ok(message, textbooks));
  }

  @Operation(summary = "교재 페이지 목록 조회", description = "특정 교재의 모든 페이지 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = PageListResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "교재를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping("{textbookId}/pages")
  public ResponseEntity<GlobalApiResponse<List<PageDto>>> findAllPageByStudentAndTextbook(
      @Parameter(description = "학생 ID", required = true)
      @PathVariable Long studentId,

      @Parameter(description = "교재 ID", required = true)
      @PathVariable Long textbookId) {

    log.info("학생({})의 교재({}) 페이지 목록 조회 요청", studentId, textbookId);

    List<PageDto> pages = studentTextbookService.getTextbookPages(studentId, textbookId);

    String message = pages.isEmpty() ? "페이지가 없습니다." : "페이지 목록 조회 성공";

    return ResponseEntity.ok(GlobalApiResponse.ok(message, pages));
  }

  @Operation(summary = "페이지 상세 조회", description = "특정 페이지의 상세 내용과 팁, 이미지를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "404", description = "페이지를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping("/pages/{pageId}")
  public ResponseEntity<GlobalApiResponse<PageDetailResponseDto>> getPageDetail(
      @Parameter(description = "학생 ID", required = true)
      @PathVariable Long studentId,

      @Parameter(description = "페이지 ID", required = true)
      @PathVariable Long pageId) {

    log.info("학생({}), 페이지({})의 상세 내용 조회 요청", studentId, pageId);

    PageDetailResponseDto pageDetail = studentTextbookService.getPageDetail(studentId, pageId);

    return ResponseEntity.ok(GlobalApiResponse.ok(pageDetail));
  }

  @Operation(summary = "페이지 진행 상태 업데이트", description = "학생의 페이지 학습 진행 상태를 업데이트합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "업데이트 성공"),
      @ApiResponse(responseCode = "404", description = "페이지를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping("/page/{pageId}/progress")
  public ResponseEntity<GlobalApiResponse<Void>> updatePageProgress(
      @Parameter(description = "학생 ID", required = true)
      @PathVariable Long studentId,

      @Parameter(description = "페이지 ID", required = true)
      @PathVariable Long pageId,

      @RequestBody PageProgressUpdateRequestDto request) {

    log.info("학생({}), 페이지({})의 진행 상태 업데이트 요청", studentId, pageId);

    studentTextbookService.updatePageProgress(studentId, pageId, request);

    return ResponseEntity.ok(GlobalApiResponse.ok("페이지 진행 상태가 업데이트되었습니다.", null));
  }

  /*@Operation(summary = "페이지 접근성 설정 업데이트", description = "학생의 페이지 접근성 설정을 업데이트합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "업데이트 성공"),
      @ApiResponse(responseCode = "404", description = "페이지를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping("/page/{pageId}/accessibility")
  public ResponseEntity<?> updateAccessibilitySettings(
      @Parameter(description = "학생 ID", required = true)
      @PathVariable Long studentId,

      @Parameter(description = "페이지 ID", required = true)
      @PathVariable Long pageId,

      @RequestBody AccessibilitySettingsUpdateRequestDto request) {

    log.info("학생 ID: {}, 페이지 ID: {}의 접근성 설정 업데이트 요청", studentId, pageId);

    try {
      return studentTextbookService.updateAccessibilitySettings(studentId, pageId, request);
    } catch (Exception e) {
      log.error("페이지 접근성 설정 업데이트 중 오류 발생", e);
      return ResponseEntity.status(500).body(
          PageListResponseDto.builder()
              .success(false)
              .message("페이지 접근성 설정 업데이트 중 오류가 발생했습니다: " + e.getMessage())
              .build()
      );
    }
  }*/
} 