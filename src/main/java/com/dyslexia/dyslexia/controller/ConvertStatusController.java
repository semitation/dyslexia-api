package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.ConvertProcessStatusDto;
import com.dyslexia.dyslexia.enums.ConvertProcessStatus;
import com.dyslexia.dyslexia.service.ConvertProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/{textbookId}")
    public ResponseEntity<ConvertProcessStatusDto> getConvertStatus(
            @Parameter(description = "교재 ID", required = true) 
            @PathVariable("textbookId") Long textbookId) {
        
        try {
            ConvertProcessStatus status = convertProcessService.getConvertProcessStatus(textbookId);
            int progress = convertProcessService.calculateConvertProcessProgress(textbookId);
            
            ConvertProcessStatusDto responseDto = ConvertProcessStatusDto.builder()
                .success(true)
                .message("교재 처리 상태 조회 성공")
                .textbookId(textbookId)
                .status(status)
                .progress(progress)
                .build();
            
            return ResponseEntity.ok(responseDto);
            
        } catch (IllegalArgumentException e) {
            log.error("교재 상태 조회 중 오류 발생", e);
            return ResponseEntity.status(404).body(
                ConvertProcessStatusDto.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            log.error("교재 상태 조회 중 오류 발생", e);
            return ResponseEntity.status(500).body(
                ConvertProcessStatusDto.builder()
                    .success(false)
                    .message("교재 상태 조회 중 오류가 발생했습니다: " + e.getMessage())
                    .build()
            );
        }
    }
    
    @Operation(summary = "교재 처리 재시도", description = "실패한 교재의 처리를 재시도합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "재시도 성공", 
                     content = @Content(schema = @Schema(implementation = ConvertProcessStatusDto.class))),
        @ApiResponse(responseCode = "400", description = "재시도할 수 없는 상태"),
        @ApiResponse(responseCode = "404", description = "교재를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/{textbookId}/retry")
    public ResponseEntity<ConvertProcessStatusDto> retryConvertProcess(
            @Parameter(description = "교재 ID", required = true) 
            @PathVariable("textbookId") Long textbookId) {
        
        log.info("교재 ID: {}의 처리 재시도 요청", textbookId);
        
        try {
            convertProcessService.retryConvertProcessing(textbookId);
            
            ConvertProcessStatusDto responseDto = ConvertProcessStatusDto.builder()
                .success(true)
                .message("교재 처리 재시도가 시작되었습니다.")
                .textbookId(textbookId)
                .status(ConvertProcessStatus.PENDING)
                .progress(0)
                .build();
            
            return ResponseEntity.ok(responseDto);
            
        } catch (IllegalArgumentException e) {
            log.error("교재 처리 재시도 중 오류 발생", e);
            return ResponseEntity.status(404).body(
                ConvertProcessStatusDto.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        } catch (IllegalStateException e) {
            log.error("교재 처리 재시도 중 오류 발생", e);
            return ResponseEntity.status(400).body(
                ConvertProcessStatusDto.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            log.error("교재 처리 재시도 중 오류 발생", e);
            return ResponseEntity.status(500).body(
                ConvertProcessStatusDto.builder()
                    .success(false)
                    .message("교재 처리 재시도 중 오류가 발생했습니다: " + e.getMessage())
                    .build()
            );
        }
    }
} 