package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.PageDetailResponseDto;
import com.dyslexia.dyslexia.dto.PageDto;
import com.dyslexia.dyslexia.dto.PageProgressUpdateRequestDto;
import com.dyslexia.dyslexia.dto.TextbookDto;
import com.dyslexia.dyslexia.dto.CommonResponse;
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
@RequestMapping("/student/textbooks")
@RequiredArgsConstructor
@Slf4j
public class StudentTextbookController {

    private final StudentTextbookService studentTextbookService;

    @Operation(summary = "내 할당된 교재 목록 조회", description = "현재 인증된 학생에게 할당된 모든 교재 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = TextbookDto.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<List<TextbookDto>>> getMyAssignedTextbooks() {
        log.info("현재 인증된 학생의 할당된 교재 목록 조회 요청");

        var textbooks = studentTextbookService.getMyAssignedTextbooks();

        String message = textbooks.isEmpty() ? "할당된 교재가 없습니다." : "할당된 교재 목록 조회 성공";

        return ResponseEntity.ok(new CommonResponse<>(message, textbooks));
    }

    @Operation(summary = "내 교재 페이지 목록 조회", description = "현재 인증된 학생의 특정 교재의 모든 페이지 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PageDto.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "교재에 대한 접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "교재를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{textbookId}/pages")
    public ResponseEntity<CommonResponse<List<PageDto>>> getMyTextbookPages(
        @Parameter(description = "교재 ID", required = true)
        @PathVariable("textbookId") Long textbookId) {

        log.info("현재 인증된 학생의 교재({}) 페이지 목록 조회 요청", textbookId);

        List<PageDto> pages = studentTextbookService.getMyTextbookPages(textbookId);

        String message = pages.isEmpty() ? "페이지가 없습니다." : "페이지 목록 조회 성공";

        return ResponseEntity.ok(new CommonResponse<>(message, pages));
    }

    @Operation(summary = "내 페이지 상세 조회", description = "현재 인증된 학생의 특정 페이지의 상세 내용과 팁, 이미지를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "페이지에 대한 접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "페이지를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/pages/{pageId}")
    public ResponseEntity<CommonResponse<PageDetailResponseDto>> getMyPageDetail(
        @Parameter(description = "페이지 ID", required = true)
        @PathVariable("pageId") Long pageId) {

        log.info("현재 인증된 학생의 페이지({}) 상세 내용 조회 요청", pageId);

        PageDetailResponseDto pageDetail = studentTextbookService.getMyPageDetail(pageId);

        return ResponseEntity.ok(new CommonResponse<>("페이지 상세 조회 성공", pageDetail));
    }

    @Operation(summary = "내 페이지 진행 상태 업데이트", description = "현재 인증된 학생의 페이지 학습 진행 상태를 업데이트합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "업데이트 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "페이지에 대한 접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "페이지를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/pages/{pageId}/progress")
    public ResponseEntity<CommonResponse<Void>> updateMyPageProgress(
        @Parameter(description = "페이지 ID", required = true)
        @PathVariable("pageId") Long pageId,

        @RequestBody PageProgressUpdateRequestDto request) {

        log.info("현재 인증된 학생의 페이지({}) 진행 상태 업데이트 요청", pageId);

        studentTextbookService.updateMyPageProgress(pageId, request);

        return ResponseEntity.ok(new CommonResponse<>("페이지 진행 상태가 업데이트되었습니다.", null));
    }

    // 레거시 API들 (하위 호환성을 위해 유지, 하지만 위의 JWT 기반 API 사용을 권장)
//    @Operation(summary = "[레거시] 학생에게 할당된 교재 목록 조회", description = "학생 ID를 통해 할당된 교재 목록을 조회합니다. (JWT 기반 /student/textbooks 사용 권장)")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "조회 성공",
//            content = @Content(schema = @Schema(implementation = TextbookDto.class))),
//        @ApiResponse(responseCode = "404", description = "학생을 찾을 수 없음"),
//        @ApiResponse(responseCode = "500", description = "서버 오류")
//    })
//    @GetMapping("/legacy/students/{studentId}")
//    public ResponseEntity<CommonResponse<List<TextbookDto>>> findAllAssignedTextbookByStudent(
//        @Parameter(description = "학생 ID", required = true)
//        @PathVariable Long studentId) {
//
//        log.info("학생({})의 할당된 교재 목록 조회 요청 (레거시)", studentId);
//
//        var textbooks = studentTextbookService.getAssignedTextbooks(studentId);
//
//        String message = textbooks.isEmpty() ? "할당된 교재가 없습니다." : "할당된 교재 목록 조회 성공";
//
//        return ResponseEntity.ok(new CommonResponse<>(message, textbooks));
//    }
//
//    @Operation(summary = "[레거시] 교재 페이지 목록 조회", description = "학생 ID와 교재 ID를 통해 페이지 목록을 조회합니다. (JWT 기반 API 사용 권장)")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "조회 성공",
//            content = @Content(schema = @Schema(implementation = PageDto.class))),
//        @ApiResponse(responseCode = "404", description = "교재를 찾을 수 없음"),
//        @ApiResponse(responseCode = "500", description = "서버 오류")
//    })
//    @GetMapping("/legacy/students/{studentId}/{textbookId}/pages")
//    public ResponseEntity<CommonResponse<List<PageDto>>> findAllPageByStudentAndTextbook(
//        @Parameter(description = "학생 ID", required = true)
//        @PathVariable Long studentId,
//
//        @Parameter(description = "교재 ID", required = true)
//        @PathVariable Long textbookId) {
//
//        log.info("학생({})의 교재({}) 페이지 목록 조회 요청 (레거시)", studentId, textbookId);
//
//        List<PageDto> pages = studentTextbookService.getTextbookPages(studentId, textbookId);
//
//        String message = pages.isEmpty() ? "페이지가 없습니다." : "페이지 목록 조회 성공";
//
//        return ResponseEntity.ok(new CommonResponse<>(message, pages));
//    }
//
//    @Operation(summary = "[레거시] 페이지 상세 조회", description = "학생 ID와 페이지 ID를 통해 페이지 상세 내용을 조회합니다. (JWT 기반 API 사용 권장)")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "조회 성공"),
//        @ApiResponse(responseCode = "404", description = "페이지를 찾을 수 없음"),
//        @ApiResponse(responseCode = "500", description = "서버 오류")
//    })
//    @GetMapping("/legacy/students/{studentId}/pages/{pageId}")
//    public ResponseEntity<CommonResponse<PageDetailResponseDto>> getPageDetail(
//        @Parameter(description = "학생 ID", required = true)
//        @PathVariable Long studentId,
//
//        @Parameter(description = "페이지 ID", required = true)
//        @PathVariable Long pageId) {
//
//        log.info("학생({}), 페이지({})의 상세 내용 조회 요청 (레거시)", studentId, pageId);
//
//        PageDetailResponseDto pageDetail = studentTextbookService.getPageDetail(studentId, pageId);
//
//        return ResponseEntity.ok(new CommonResponse<>("페이지 상세 조회 성공", pageDetail));
//    }
//
//    @Operation(summary = "[레거시] 페이지 진행 상태 업데이트", description = "학생 ID와 페이지 ID를 통해 진행 상태를 업데이트합니다. (JWT 기반 API 사용 권장)")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "업데이트 성공"),
//        @ApiResponse(responseCode = "404", description = "페이지를 찾을 수 없음"),
//        @ApiResponse(responseCode = "500", description = "서버 오류")
//    })
//    @PostMapping("/legacy/students/{studentId}/pages/{pageId}/progress")
//    public ResponseEntity<CommonResponse<Void>> updatePageProgress(
//        @Parameter(description = "학생 ID", required = true)
//        @PathVariable Long studentId,
//
//        @Parameter(description = "페이지 ID", required = true)
//        @PathVariable Long pageId,
//
//        @RequestBody PageProgressUpdateRequestDto request) {
//
//        log.info("학생({}), 페이지({})의 진행 상태 업데이트 요청 (레거시)", studentId, pageId);
//
//        studentTextbookService.updatePageProgress(studentId, pageId, request);
//
//        return ResponseEntity.ok(new CommonResponse<>("페이지 진행 상태가 업데이트되었습니다.", null));
//    }
}
