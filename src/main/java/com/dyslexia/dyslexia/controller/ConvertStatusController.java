package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.ConvertProcessStatusDto;
import com.dyslexia.dyslexia.enums.ConvertProcessStatus;
import com.dyslexia.dyslexia.dto.CommonResponse;
import com.dyslexia.dyslexia.service.ConvertProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "TextbookStatus", description = "교재 처리 상태 관리 API")
@RestController
@RequestMapping("textbooks/status")
@RequiredArgsConstructor
@Slf4j
public class ConvertStatusController {

  private final ConvertProcessService convertProcessService;

  @Operation(summary = "교재 처리 상태 조회", description = "교재의 처리 상태와 진행도를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = ConvertProcessStatusDto.class))),
      @ApiResponse(responseCode = "404", description = "교재를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping("{textbookId}")
  public ResponseEntity<CommonResponse<ConvertProcessStatusDto>> getConvertStatus(
      @Parameter(description = "교재 ID", required = true)
      @PathVariable("textbookId") Long textbookId) {

    ConvertProcessStatus status = convertProcessService.getConvertProcessStatus(textbookId);
    int progress = convertProcessService.calculateConvertProcessProgress(textbookId);

    ConvertProcessStatusDto dto = ConvertProcessStatusDto.builder()
        .textbookId(textbookId)
        .status(status)
        .progress(progress)
        .build();

    return ResponseEntity.ok(new CommonResponse<>("교재 처리 상태 조회 성공", dto));

  }

  @Operation(summary = "교재 처리 재시도", description = "실패한 교재의 처리를 재시도합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "재시도 성공",
          content = @Content(schema = @Schema(implementation = ConvertProcessStatusDto.class))),
      @ApiResponse(responseCode = "400", description = "재시도할 수 없는 상태"),
      @ApiResponse(responseCode = "404", description = "교재를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping("{textbookId}/retry")
  public ResponseEntity<CommonResponse<ConvertProcessStatusDto>> retryConvertProcess(
      @Parameter(description = "교재 ID", required = true)
      @PathVariable("textbookId") Long textbookId) throws IOException {

    log.info("교재 ID: {}의 처리 재시도 요청", textbookId);

    convertProcessService.retryConvertProcessing(textbookId);

    ConvertProcessStatusDto dto = ConvertProcessStatusDto.builder()
        .textbookId(textbookId)
        .status(ConvertProcessStatus.PENDING)
        .progress(0)
        .build();

    return ResponseEntity.ok(new CommonResponse<>("교재 처리 재시도가 시작되었습니다", dto));
  }
} 