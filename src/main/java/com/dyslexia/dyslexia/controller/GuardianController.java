package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.CommonResponse;
import com.dyslexia.dyslexia.dto.StudentDto;
import com.dyslexia.dyslexia.dto.GuardianCodeDto;
import com.dyslexia.dyslexia.dto.GuardianDto;
import com.dyslexia.dyslexia.service.StudentService;
import com.dyslexia.dyslexia.service.GuardianService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Guardian", description = "보호자 관련 API")
@RestController
@RequestMapping("/guardian")
@RequiredArgsConstructor
public class GuardianController {

  private final GuardianService guardianService;
  private final StudentService studentService;

  @Operation(summary = "내 정보 조회", description = "현재 인증된 보호자의 정보를 조회합니다.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "조회 성공",
      content = @Content(schema = @Schema(implementation = GuardianDto.class))),
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
  })
  @GetMapping("/me")
  public ResponseEntity<CommonResponse<GuardianDto>> getMyInfo() {
    GuardianDto guardian = guardianService.getMyInfo();
    return ResponseEntity.ok(new CommonResponse<>("보호자 정보 조회 성공", guardian));
  }

  @Operation(summary = "내 매칭 코드 조회", description = "현재 인증된 보호자의 매칭 코드를 조회합니다.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "조회 성공",
      content = @Content(schema = @Schema(implementation = GuardianCodeDto.class))),
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
  })
  @GetMapping("/code")
  public ResponseEntity<CommonResponse<GuardianCodeDto>> getMyCode() {
    GuardianCodeDto code = guardianService.getMyCode();
    return ResponseEntity.ok(new CommonResponse<>("매칭 코드 조회 성공", code));
  }

  @Operation(summary = "내 담당 학생 목록 조회", description = "현재 인증된 보호자의 담당 학생 목록을 조회합니다.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "조회 성공",
      content = @Content(schema = @Schema(implementation = StudentDto.class))),
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
  })
  @GetMapping("/students")
  public ResponseEntity<CommonResponse<List<StudentDto>>> getMyStudents() {
    List<StudentDto> students = guardianService.getMyStudents();
    return ResponseEntity.ok(new CommonResponse<>(
        students.isEmpty() ? "담당 학생이 없습니다." : "담당 학생 목록 조회 성공", 
        students
    ));
  }

  // 레거시 API들 (하위 호환성을 위해 유지, 하지만 위의 JWT 기반 API 사용을 권장)
//  @Operation(summary = "[레거시] id로 보호자 조회", description = "보호자 ID를 통해 보호자 정보를 조회합니다. (JWT 기반 /guardian/me 사용 권장)")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "조회 성공",
//      content = @Content(schema = @Schema(implementation = GuardianDto.class))),
//  })
//  @GetMapping("/{id}")
//  public ResponseEntity<CommonResponse<GuardianDto>> getById(
//      @Parameter(description = "보호자 ID", required = true) @PathVariable long id) {
//    GuardianDto guardian = guardianService.getById(id);
//    return ResponseEntity.ok(new CommonResponse<>("보호자 조회 성공", guardian));
//  }
//
//  @Operation(summary = "[레거시] 클라이언트 ID로 보호자 조회", description = "클라이언트 ID를 통해 보호자 정보를 조회합니다. (JWT 기반 /guardian/me 사용 권장)")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "조회 성공",
//      content = @Content(schema = @Schema(implementation = GuardianDto.class))),
//  })
//  @GetMapping("/by-client-id")
//  public ResponseEntity<CommonResponse<GuardianDto>> getByClientId(
//      @Parameter(description = "클라이언트 ID", required = true) @RequestParam String clientId) {
//    GuardianDto guardian = guardianService.getGuardianByClientId(clientId);
//    return ResponseEntity.ok(new CommonResponse<>("보호자 조회 성공", guardian));
//  }
//
//  @Operation(summary = "[레거시] id로 매칭 코드 조회", description = "보호자 ID를 통해 매칭 코드를 조회합니다. (JWT 기반 /guardian/code 사용 권장)")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "조회 성공",
//      content = @Content(schema = @Schema(implementation = GuardianCodeDto.class))),
//  })
//  @GetMapping("/{id}/code")
//  public ResponseEntity<CommonResponse<GuardianCodeDto>> getCodeById(
//      @Parameter(description = "보호자 ID", required = true) @PathVariable long id) {
//    GuardianCodeDto code = guardianService.getCodeById(id);
//    return ResponseEntity.ok(new CommonResponse<>("매칭 코드 조회 성공", code));
//  }
//
//  @Operation(summary = "[레거시] id로 담당 학생 조회", description = "보호자 ID를 통해 담당 학생 목록을 조회합니다. (JWT 기반 /guardian/students 사용 권장)")
//  @ApiResponses(value = {
//    @ApiResponse(responseCode = "200", description = "조회 성공",
//      content = @Content(schema = @Schema(implementation = StudentDto.class))),
//  })
//  @GetMapping("/{guardianId}/students")
//  public ResponseEntity<CommonResponse<List<StudentDto>>> getStudentsByGuardianId(
//      @Parameter(description = "보호자 ID", required = true) @PathVariable Long guardianId) {
//    List<StudentDto> students = studentService.getStudentsByGuardian(guardianId);
//    return ResponseEntity.ok(new CommonResponse<>("담당 학생 목록 조회 성공", students));
//  }
}
