package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.CommonResponse;
import com.dyslexia.dyslexia.dto.TextbookDetailResponseDto;
import com.dyslexia.dyslexia.dto.TextbookDto;
import com.dyslexia.dyslexia.service.GuardianTextbookService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "GuardianTextbook", description = "보호자 교재 관리 API")
@RestController
@RequestMapping("/guardian/textbooks")
@RequiredArgsConstructor
@Slf4j
public class GuardianTextbookController {

    private final GuardianTextbookService guardianTextbookService;

    @Operation(summary = "내 교재 목록 조회", description = "현재 인증된 보호자가 업로드한 모든 교재 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = TextbookDto.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<List<TextbookDto>>> getMyTextbooks() {
        log.info("내 교재 목록 조회 요청");

        List<TextbookDto> textbooks = guardianTextbookService.getMyTextbooks();

        return ResponseEntity.ok(
            new CommonResponse<>(
                textbooks.isEmpty() ? "업로드한 교재가 없습니다." : "교재 목록 조회 성공",
                textbooks
            )
        );
    }

    @Operation(summary = "교재 상세 정보 조회", description = "현재 인증된 보호자의 특정 교재 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = TextbookDetailResponseDto.class))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (본인의 교재가 아님)"),
        @ApiResponse(responseCode = "404", description = "교재를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{textbookId}/detail")
    public ResponseEntity<CommonResponse<TextbookDetailResponseDto>> getTextbookDetail(@PathVariable("textbookId") Long textbookId) {

        log.info("교재 상세 정보 조회 요청: 교재 ID({})", textbookId);

        TextbookDetailResponseDto textbookDetail = guardianTextbookService.getTextbookDetail(textbookId);

        return ResponseEntity.ok(
            new CommonResponse<>("교재 상세 정보 조회 성공", textbookDetail)
        );
    }

    @Operation(summary = "학생에게 교재 할당", description = "현재 인증된 보호자가 학생에게 교재를 할당합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "할당 성공"),
        @ApiResponse(responseCode = "400", description = "이미 할당된 교재"),
        @ApiResponse(responseCode = "404", description = "학생 또는 교재를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/assign")
    public ResponseEntity<CommonResponse<Void>> assignTextbook(
        @Parameter(description = "학생 ID", required = true) @RequestParam("studentId") Long studentId,
        @Parameter(description = "교재 ID", required = true) @RequestParam("textbookId") Long textbookId,
        @Parameter(description = "할당 메모") @RequestParam(name = "notes", required = false) String notes) {

        log.info("교재 할당 요청: 학생({}), 교재({})", studentId, textbookId);

        guardianTextbookService.assignTextbookToStudent(studentId, textbookId, notes);

        return ResponseEntity.ok(new CommonResponse<>("교재가 학생에게 할당되었습니다."));
    }

    @Operation(summary = "학생 교재 할당 취소", description = "현재 인증된 보호자가 학생에게 할당한 교재를 취소합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "취소 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (본인이 할당한 교재가 아님)"),
        @ApiResponse(responseCode = "404", description = "할당 정보를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/assign/{studentId}/{textbookId}")
    public ResponseEntity<CommonResponse<Void>> unassignTextbook(
        @Parameter(description = "학생 ID", required = true) @PathVariable("studentId") Long studentId,
        @Parameter(description = "교재 ID", required = true) @PathVariable("textbookId") Long textbookId) {

        log.info("교재 할당 취소 요청: 학생({}), 교재({})", studentId, textbookId);

        guardianTextbookService.unassignTextbookFromStudent(studentId, textbookId);

        return ResponseEntity.ok(new CommonResponse<>("교재 할당이 취소되었습니다."));
    }

    @Operation(summary = "학생별 할당 교재 목록 조회", description = "현재 인증된 보호자가 특정 학생에게 할당한 교재 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = TextbookDto.class))),
        @ApiResponse(responseCode = "404", description = "학생을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/assigned/{studentId}")
    public ResponseEntity<CommonResponse<List<TextbookDto>>> getAssignedTextbooks(
        @Parameter(description = "학생 ID", required = true) @PathVariable("studentId") Long studentId) {

        log.info("학생({})의 할당된 교재 목록 조회 요청", studentId);

        List<TextbookDto> textbooks = guardianTextbookService.getAssignedTextbooksForStudent(studentId);

        return ResponseEntity.ok(
            new CommonResponse<>(
                textbooks.isEmpty() ? "할당된 교재가 없습니다." : "할당된 교재 목록 조회 성공",
                textbooks
            )
        );
    }
}
