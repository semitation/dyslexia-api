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
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Guardian", description = "보호자 관련 API")
@RestController
@RequestMapping("/guardians")
@RequiredArgsConstructor
public class GuardianController {

  private final GuardianService guardianService;
  private final StudentService studentService;

  @Operation(summary = "id로 보호자 조회", description = "보호자 ID를 통해 보호자 정보를 조회합니다.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "조회 성공",
      content = @Content(schema = @Schema(implementation = GuardianDto.class))),
  })
  @GetMapping("/{id}")
  public ResponseEntity<CommonResponse<GuardianDto>> getById(
      @Parameter(description = "보호자 ID", required = true) @PathVariable long id) {
    GuardianDto guardian = guardianService.getById(id);
    return ResponseEntity.ok(new CommonResponse<>("보호자 조회 성공", guardian));
  }

  @Operation(summary = "클라이언트 ID로 보호자 조회", description = "클라이언트 ID를 통해 보호자 정보를 조회합니다.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "조회 성공",
      content = @Content(schema = @Schema(implementation = GuardianDto.class))),
  })

  @GetMapping
  public ResponseEntity<CommonResponse<GuardianDto>> getByClientId(
      @Parameter(description = "클라이언트 ID", required = true) @RequestParam String clientId) {
    GuardianDto guardian = guardianService.getGuardianByClientId(clientId);
    return ResponseEntity.ok(new CommonResponse<>("보호자 조회 성공", guardian));
  }

  @Operation(summary = "id로 매칭 코드 조회", description = "보호자 ID를 통해 매칭 코드를 조회합니다.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "조회 성공",
      content = @Content(schema = @Schema(implementation = GuardianCodeDto.class))),
  })
  @GetMapping("/code/{id}")
  public ResponseEntity<CommonResponse<GuardianCodeDto>> getCodeById(
      @Parameter(description = "보호자 ID", required = true) @PathVariable long id) {
    GuardianCodeDto code = guardianService.getCodeById(id);
    return ResponseEntity.ok(new CommonResponse<>("매칭 코드 조회 성공", code));
  }

  @Operation(summary = "id로 담당 학생 조회", description = "보호자 ID를 통해 담당 학생 목록을 조회합니다.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "조회 성공",
      content = @Content(schema = @Schema(implementation = StudentDto.class))),
  })
  @GetMapping("/{guardianId}/students")
  public ResponseEntity<CommonResponse<List<StudentDto>>> getStudentsByGuardianId(
      @Parameter(description = "보호자 ID", required = true) @PathVariable Long guardianId) {
    List<StudentDto> students = studentService.getStudentsByGuardian(guardianId);
    return ResponseEntity.ok(new CommonResponse<>("담당 학생 목록 조회 성공", students));
  }
}
