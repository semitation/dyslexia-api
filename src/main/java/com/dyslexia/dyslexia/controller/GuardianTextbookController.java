package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.ResponseDto;
import com.dyslexia.dyslexia.dto.TextbookAssignmentRequestDto;
import com.dyslexia.dyslexia.dto.TextbookDto;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.entity.StudentTextbookAssignment;
import com.dyslexia.dyslexia.entity.Textbook;
import com.dyslexia.dyslexia.exception.GlobalApiResponse;
import com.dyslexia.dyslexia.mapper.TextbookMapper;
import com.dyslexia.dyslexia.repository.GuardianRepository;
import com.dyslexia.dyslexia.repository.StudentRepository;
import com.dyslexia.dyslexia.repository.StudentTextbookAssignmentRepository;
import com.dyslexia.dyslexia.repository.TextbookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "GuardianTextbook", description = "보호자 교재 관리 API")
@RestController
@RequestMapping("guardians/{guardianId}")
@RequiredArgsConstructor
@Slf4j
public class GuardianTextbookController {

  private final GuardianRepository guardianRepository;
  private final TextbookRepository textbookRepository;
  private final StudentRepository studentRepository;
  private final StudentTextbookAssignmentRepository assignmentRepository;

  private TextbookMapper textbookMapper;

  @Operation(summary = "보호자가 업로드한 교재 목록 조회", description = "보호자가 업로드한 모든 교재 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = TextbookDto.class))),
      @ApiResponse(responseCode = "404", description = "보호자를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping("textbooks")
  public ResponseEntity<GlobalApiResponse<List<TextbookDto>>> getTeacherDocuments(
      @Parameter(description = "보호자 ID", required = true)
      @PathVariable("guardianId") Long guardianId) {

    log.info("보호자({})의 교재 목록 조회 요청", guardianId);

    // 보호자 유효성 검사
    guardianRepository.findById(guardianId)
        .orElseThrow(() -> new IllegalArgumentException("보호자를 찾을 수 없습니다."));

    // 해당 보호자의 교재 조회
    List<Textbook> textbooks = textbookRepository.findByGuardianIdOrderByUpdatedAtDesc(guardianId);

    List<TextbookDto> dtos = textbooks.stream()
        .map(textbookMapper::toDto)
        .toList();

    return ResponseEntity.ok(
        GlobalApiResponse.ok(
            dtos.isEmpty() ? "업로드한 교재가 없습니다." : "교재 목록 조회 성공",
            dtos
        )
    );
  }

  @Operation(summary = "학생에게 교재 할당", description = "보호자가 학생에게 교재를 할당합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "할당 성공",
          content = @Content(schema = @Schema(implementation = ResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "보호자, 학생 또는 교재를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping("/assign")
  public ResponseEntity<GlobalApiResponse<Void>> assignTextbook(
      @RequestBody TextbookAssignmentRequestDto request) {

    log.info("교재 할당 요청: 보호자({}) 학생({}) 교재({})",
        request.getGuardianId(), request.getStudentId(), request.getTextbookId());

    // 선생님 존재 여부 확인
    Guardian guardian = guardianRepository.findById(request.getGuardianId())
        .orElseThrow(() -> new IllegalArgumentException("보호자를 찾을 수 없습니다."));

    // 학생 존재 여부 확인
    Student student = studentRepository.findById(request.getStudentId())
        .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

    // 교재 존재 여부 확인
    Textbook textbook = textbookRepository.findById(request.getTextbookId())
        .orElseThrow(() -> new IllegalArgumentException("교재를 찾을 수 없습니다."));

    assignmentRepository.findByStudentIdAndTextbookId(
        request.getStudentId(), request.getTextbookId()).ifPresent(a -> {
      throw new IllegalStateException("이미 학생에게 할당된 교재입니다.");
    });

    // 할당 정보 생성
    StudentTextbookAssignment assignment = StudentTextbookAssignment.builder()
        .student(student)
        .textbook(textbook)
        .assignedBy(guardian)
        .assignedAt(LocalDateTime.now())
        .notes(request.getNotes())
        .build();

    // 저장
    assignmentRepository.save(assignment);

    return ResponseEntity.ok(GlobalApiResponse.ok("교재가 학생에게 할당되었습니다."));
  }

  @Operation(summary = "학생 교재 할당 취소", description = "보호자가 학생에게 할당한 교재를 취소합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "취소 성공",
          content = @Content(schema = @Schema(implementation = ResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "할당 정보를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @DeleteMapping("assign/{studentId}/{textbookId}")
  public ResponseEntity<GlobalApiResponse<Void>> unassignTextbook(
      @Parameter(description = "보호자 ID", required = true) @PathVariable Long guardianId,
      @Parameter(description = "학생 ID", required = true) @PathVariable Long studentId,
      @Parameter(description = "교재 ID", required = true) @PathVariable Long textbookId) {

    log.info("교재 할당 취소 요청: 보호자({}), 학생({}), 교재({})", guardianId, studentId, textbookId);
    // 할당 정보 조회
    StudentTextbookAssignment assignment = assignmentRepository
        .findByStudentIdAndTextbookId(studentId, textbookId)
        .orElseThrow(() -> new IllegalArgumentException("할당된 교재를 찾을 수 없습니다."));

    if (!assignment.getGuardian().getId().equals(guardianId)) {
      throw new AccessDeniedException("이 교재를 할당한 보호자만 취소할 수 있습니다.");
    }

    // 삭제
    assignmentRepository.delete(assignment);

    return ResponseEntity.ok(GlobalApiResponse.ok("교재 할당이 취소되었습니다."));
  }

  @Operation(summary = "보호자의 학생별 할당 교재 목록 조회", description = "보호자가 특정 학생에게 할당한 교재 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = StudentTextbooksResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "보호자 또는 학생을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping("students/{studentId}")
  public ResponseEntity<GlobalApiResponse<List<TextbookDto>>> getAssignedTextbooks(
      @Parameter(description = "보호자 ID", required = true) @PathVariable Long guardianId,
      @Parameter(description = "학생 ID", required = true) @PathVariable Long studentId) {

    log.info("보호자({}), 학생({})의 할당된 교재 목록 조회 요청", guardianId, studentId);

    // 할당된 교재 조회
    List<StudentTextbookAssignment> assignments =
        assignmentRepository.findByAssignedByIdAndStudentId(guardianId, studentId);

    List<TextbookDto> dtos = assignments.stream()
        .map(StudentTextbookAssignment::getTextbook)
        .map(textbookMapper::toDto)
        .toList();

    return ResponseEntity.ok(
        GlobalApiResponse.ok(
            dtos.isEmpty() ? "할당된 교재가 없습니다." : "할당된 교재 목록 조회 성공",
            dtos
        )
    );
  }
} 