package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.CommonResponse;
import com.dyslexia.dyslexia.dto.GuardianDto;
import com.dyslexia.dyslexia.dto.MatchResponseDto;
import com.dyslexia.dyslexia.dto.StudentDto;
import com.dyslexia.dyslexia.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Student", description = "학생 관련 API")
@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

  private final StudentService studentService;

  @Operation(summary = "내 정보 조회", description = "현재 인증된 학생의 정보를 조회합니다.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "조회 성공",
        content = @Content(schema = @Schema(implementation = StudentDto.class))),
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
  })
  @GetMapping("/me")
  public ResponseEntity<CommonResponse<StudentDto>> getMyInfo() {
    StudentDto student = studentService.getMyInfo();
    return ResponseEntity.ok(new CommonResponse<>("학생 정보 조회 성공", student));
  }

  @Operation(summary = "매칭 코드로 보호자 매칭", description = "현재 인증된 학생이 매칭 코드를 사용하여 보호자와 연결됩니다.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "매칭 성공",
        content = @Content(schema = @Schema(implementation = MatchResponseDto.class))),
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
    @ApiResponse(responseCode = "404", description = "매칭 코드에 해당하는 보호자를 찾을 수 없음"),
  })
  @PostMapping("/match")
  public ResponseEntity<CommonResponse<MatchResponseDto>> matchWithGuardian(
      @Parameter(description = "매칭 코드", required = true) @RequestParam("code") String code) {
    MatchResponseDto result = studentService.matchWithGuardian(code);
    return ResponseEntity.ok(new CommonResponse<>("보호자 매칭 성공", result));
  }

  @Operation(summary = "내 보호자 정보 조회", description = "현재 인증된 학생의 보호자 정보를 조회합니다.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "조회 성공",
        content = @Content(schema = @Schema(implementation = GuardianDto.class))),
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
    @ApiResponse(responseCode = "404", description = "연결된 보호자가 없음"),
  })
  @GetMapping("/guardian")
  public ResponseEntity<CommonResponse<GuardianDto>> getMyGuardian() {
    GuardianDto guardian = studentService.getMyGuardian();
    return ResponseEntity.ok(new CommonResponse<>("보호자 정보 조회 성공", guardian));
  }

//  // 레거시 API들 (하위 호환성을 위해 유지, 하지만 위의 JWT 기반 API 사용을 권장)
//  @Operation(summary = "[레거시] id로 학생 조회", description = "학생 ID를 통해 학생 정보를 조회합니다. (JWT 기반 /student/me 사용 권장)")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "조회 성공",
//        content = @Content(schema = @Schema(implementation = StudentDto.class))),
//  })
//  @GetMapping("/{id}")
//  public ResponseEntity<CommonResponse<StudentDto>> getById(
//      @Parameter(description = "학생 ID", required = true) @PathVariable Long id) {
//    StudentDto student = studentService.getById(id);
//    return ResponseEntity.ok(new CommonResponse<>("학생 조회 성공", student));
//  }
//
//  @Operation(summary = "[레거시] client id로 학생 조회", description = "클라이언트 ID를 통해 학생 정보를 조회합니다. (JWT 기반 /student/me 사용 권장)")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "조회 성공",
//        content = @Content(schema = @Schema(implementation = StudentDto.class))),
//  })
//  @GetMapping("/by-client-id")
//  public ResponseEntity<CommonResponse<StudentDto>> getByClientId(
//      @Parameter(description = "클라이언트 ID", required = true) @RequestParam String clientId) {
//    StudentDto student = studentService.getByClientId(clientId);
//    return ResponseEntity.ok(new CommonResponse<>("학생 조회 성공", student));
//  }
//
//  @Operation(summary = "[레거시] 매칭 코드로 보호자 매칭", description = "매칭 코드를 사용하여 학생과 보호자를 연결합니다. (JWT 기반 /student/match 사용 권장)")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "매칭 성공",
//        content = @Content(schema = @Schema(implementation = MatchResponseDto.class))),
//  })
//  @PostMapping("/match/{id}")
//  public ResponseEntity<CommonResponse<MatchResponseDto>> matchByCode(
//      @Parameter(description = "학생 ID", required = true) @PathVariable Long id,
//      @Parameter(description = "매칭 코드", required = true) @RequestParam String code) {
//    MatchResponseDto result = studentService.matchByCode(id, code);
//    return ResponseEntity.ok(new CommonResponse<>("보호자 매칭 성공", result));
//  }
}
