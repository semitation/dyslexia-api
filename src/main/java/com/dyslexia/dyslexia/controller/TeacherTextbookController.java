package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.DocumentAssignmentRequestDto;
import com.dyslexia.dyslexia.dto.ResponseDto;
import com.dyslexia.dyslexia.dto.StudentTextbookListResponseDto;
import com.dyslexia.dyslexia.dto.TextbookDto;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.entity.StudentTextbookAssignment;
import com.dyslexia.dyslexia.entity.Textbook;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "TeacherDocument", description = "선생님 교재 관리 API")
@RestController
@RequestMapping("teacher/textbooks")
@RequiredArgsConstructor
@Slf4j
public class TeacherTextbookController {

  private final GuardianRepository guardianRepository;
  private final TextbookRepository textbookRepository;
  private final StudentRepository studentRepository;
  private final StudentTextbookAssignmentRepository assignmentRepository;

  @Operation(summary = "선생님이 업로드한 교재 목록 조회", description = "선생님이 업로드한 모든 교재 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = StudentTextbookListResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "선생님을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping("/{teacherId}")
  public ResponseEntity<StudentTextbookListResponseDto> getTeacherDocuments(
      @Parameter(description = "선생님 ID", required = true)
      @PathVariable("teacherId") Long teacherId) {

    log.info("선생님 ID: {}의 교재 목록 조회 요청", teacherId);

    try {
      List<Textbook> textbooks = textbookRepository.findAllByOrderByUpdatedAtDesc();

      if (textbooks.isEmpty()) {
        return ResponseEntity.ok(
            StudentTextbookListResponseDto.builder()
                .success(true)
                .message("업로드한 교재가 없습니다.")
                .textbooks(List.of())
                .build()
        );
      }

      List<TextbookDto> textbookDtos = textbooks.stream()
          .map(textbook -> TextbookDto.builder()
              .id(textbook.getId())
              .guardianId(textbook.getGuardian().getId())
              .title(textbook.getTitle())
              .createdAt(textbook.getCreatedAt())
              .updatedAt(textbook.getUpdatedAt())
              .build())
          .toList();

      return ResponseEntity.ok(
          StudentTextbookListResponseDto.builder()
              .success(true)
              .message("교재 목록 조회 성공")
              .textbooks(textbookDtos)
              .build()
      );

    } catch (Exception e) {
      log.error("선생님 교재 목록 조회 중 오류 발생", e);
      return ResponseEntity.status(500).body(
          StudentTextbookListResponseDto.builder()
              .success(false)
              .message("교재 목록 조회 중 오류가 발생했습니다: " + e.getMessage())
              .build()
      );
    }
  }

  @Operation(summary = "학생에게 교재 할당", description = "선생님이 학생에게 교재를 할당합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "할당 성공",
          content = @Content(schema = @Schema(implementation = ResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "선생님, 학생 또는 교재를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping("/assign")
  public ResponseEntity<ResponseDto> assignTextbook(
      @RequestBody DocumentAssignmentRequestDto request) {

    log.info("교재 할당 요청: 보호자({}) 학생({}) 교재({})",
        request.getGuardianId(), request.getStudentId(), request.getTextbookId());

    try {
      // 선생님 존재 여부 확인
      Guardian guardian = guardianRepository.findById(request.getGuardianId())
          .orElseThrow(() -> new IllegalArgumentException("선생님을 찾을 수 없습니다."));

      // 학생 존재 여부 확인
      Student student = studentRepository.findById(request.getStudentId())
          .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

      // 교재 존재 여부 확인
      Textbook textbook = textbookRepository.findById(request.getTextbookId())
          .orElseThrow(() -> new IllegalArgumentException("교재를 찾을 수 없습니다."));

      // 이미 할당되었는지 확인
      boolean alreadyAssigned = assignmentRepository.findByStudentIdAndTextbookId(
          request.getStudentId(), request.getTextbookId()).isPresent();

      if (alreadyAssigned) {
        return ResponseEntity.ok(
            ResponseDto.builder()
                .success(true)
                .message("이미 학생에게 할당된 교재입니다.")
                .build()
        );
      }

      // 할당 정보 생성
      StudentTextbookAssignment assignment = StudentTextbookAssignment.builder()
          .student(student)
          .textbook(textbook)
          .assignedBy(guardian)
          .assignedAt(LocalDateTime.now())
          .dueDate(request.getDueDate())
          .notes(request.getNotes())
          .build();

      // 저장
      assignmentRepository.save(assignment);

      return ResponseEntity.ok(
          ResponseDto.builder()
              .success(true)
              .message("교재가 학생에게 할당되었습니다.")
              .build()
      );

    } catch (IllegalArgumentException e) {
      log.error("교재 할당 중 오류 발생", e);
      return ResponseEntity.status(404).body(
          ResponseDto.builder()
              .success(false)
              .message(e.getMessage())
              .build()
      );
    } catch (Exception e) {
      log.error("교재 할당 중 오류 발생", e);
      return ResponseEntity.status(500).body(
          ResponseDto.builder()
              .success(false)
              .message("교재 할당 중 오류가 발생했습니다: " + e.getMessage())
              .build()
      );
    }
  }

  @Operation(summary = "학생 교재 할당 취소", description = "선생님이 학생에게 할당한 교재를 취소합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "취소 성공",
          content = @Content(schema = @Schema(implementation = ResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "할당 정보를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @DeleteMapping("/assign/{teacherId}/{studentId}/{textbookId}")
  public ResponseEntity<ResponseDto> unassignDocumentFromStudent(
      @Parameter(description = "선생님 ID", required = true)
      @PathVariable Long teacherId,

      @Parameter(description = "학생 ID", required = true)
      @PathVariable Long studentId,

      @Parameter(description = "교재 ID", required = true)
      @PathVariable Long textbookId) {

    log.info("교재 할당 취소 요청: 선생님 ID: {}, 학생 ID: {}, 교재 ID: {}",
        teacherId, studentId, textbookId);

    try {
      // 할당 정보 조회
      StudentTextbookAssignment assignment = assignmentRepository
          .findByStudentIdAndTextbookId(studentId, textbookId)
          .orElseThrow(() -> new IllegalArgumentException("할당된 교재를 찾을 수 없습니다."));

      // 할당한 선생님인지 확인
      if (!assignment.getGuardian().getId().equals(teacherId)) {
        return ResponseEntity.status(403).body(
            ResponseDto.builder()
                .success(false)
                .message("이 교재를 할당한 선생님만 취소할 수 있습니다.")
                .build()
        );
      }

      // 삭제
      assignmentRepository.delete(assignment);

      return ResponseEntity.ok(
          ResponseDto.builder()
              .success(true)
              .message("교재 할당이 취소되었습니다.")
              .build()
      );

    } catch (IllegalArgumentException e) {
      log.error("교재 할당 취소 중 오류 발생", e);
      return ResponseEntity.status(404).body(
          ResponseDto.builder()
              .success(false)
              .message(e.getMessage())
              .build()
      );
    } catch (Exception e) {
      log.error("교재 할당 취소 중 오류 발생", e);
      return ResponseEntity.status(500).body(
          ResponseDto.builder()
              .success(false)
              .message("교재 할당 취소 중 오류가 발생했습니다: " + e.getMessage())
              .build()
      );
    }
  }

  @Operation(summary = "선생님의 학생별 할당 교재 목록 조회", description = "선생님이 특정 학생에게 할당한 교재 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = StudentTextbookListResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "선생님 또는 학생을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping("/{teacherId}/students/{studentId}")
  public ResponseEntity<StudentTextbookListResponseDto> getAssignedDocumentsForStudent(
      @Parameter(description = "선생님 ID", required = true)
      @PathVariable Long teacherId,

      @Parameter(description = "학생 ID", required = true)
      @PathVariable Long studentId) {

    log.info("선생님 ID: {}, 학생 ID: {}의 할당된 교재 목록 조회 요청", teacherId, studentId);

    try {
      List<StudentTextbookAssignment> assignments = assignmentRepository
          .findByAssignedByIdAndStudentId(teacherId, studentId);

      if (assignments.isEmpty()) {
        return ResponseEntity.ok(
            StudentTextbookListResponseDto.builder()
                .success(true)
                .message("할당된 교재가 없습니다.")
                .textbooks(List.of())
                .build()
        );
      }

      List<TextbookDto> textbooks = assignments.stream()
          .map(assignment -> {
            Textbook textbook = assignment.getTextbook();
            return TextbookDto.builder()
                .id(textbook.getId())
                .guardianId(textbook.getGuardian().getId())
                .title(textbook.getTitle())
                .pageCount(textbook.getPageCount())
                .createdAt(textbook.getCreatedAt())
                .updatedAt(textbook.getUpdatedAt())
                .build();
          })
          .toList();

      return ResponseEntity.ok(
          StudentTextbookListResponseDto.builder()
              .success(true)
              .message("할당된 교재 목록 조회 성공")
              .textbooks(textbooks)
              .build()
      );

    } catch (Exception e) {
      log.error("할당된 교재 목록 조회 중 오류 발생", e);
      return ResponseEntity.status(500).body(
          StudentTextbookListResponseDto.builder()
              .success(false)
              .message("할당된 교재 목록 조회 중 오류가 발생했습니다: " + e.getMessage())
              .build()
      );
    }
  }
} 