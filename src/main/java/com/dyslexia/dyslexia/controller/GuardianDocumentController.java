package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.DocumentAssignmentRequestDto;
import com.dyslexia.dyslexia.dto.DocumentDto;
import com.dyslexia.dyslexia.dto.ResponseDto;
import com.dyslexia.dyslexia.dto.StudentDocumentListResponseDto;
import com.dyslexia.dyslexia.entity.Document;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.entity.StudentDocumentAssignment;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.repository.DocumentRepository;
import com.dyslexia.dyslexia.repository.StudentDocumentAssignmentRepository;
import com.dyslexia.dyslexia.repository.StudentRepository;
import com.dyslexia.dyslexia.repository.GuardianRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "GuardianDocument", description = "보호자 문서 관리 API")
@RestController
@RequestMapping("guardian/documents")
@RequiredArgsConstructor
@Slf4j
public class GuardianDocumentController {

    private final DocumentRepository documentRepository;
    private final GuardianRepository guardianRepository;
    private final StudentRepository studentRepository;
    private final StudentDocumentAssignmentRepository assignmentRepository;

    @Operation(summary = "보호자가 업로드한 문서 목록 조회", description = "보호자가 업로드한 모든 문서 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", 
                     content = @Content(schema = @Schema(implementation = StudentDocumentListResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "보호자를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{guardianId}")
    public ResponseEntity<StudentDocumentListResponseDto> getGuardianDocuments(
            @Parameter(description = "보호자 ID", required = true) 
            @PathVariable("guardianId") Long guardianId) {
        
        log.info("보호자 ID: {}의 문서 목록 조회 요청", guardianId);
        
        try {
            List<Document> documents = documentRepository.findAllByOrderByUploadedAtDesc();
            
            if (documents.isEmpty()) {
                return ResponseEntity.ok(
                    StudentDocumentListResponseDto.builder()
                        .success(true)
                        .message("업로드한 문서가 없습니다.")
                        .documents(List.of())
                        .build()
                );
            }
            
            List<DocumentDto> documentDtos = documents.stream()
                .map(document -> DocumentDto.builder()
                    .id(document.getId())
                    .guardianId(document.getGuardian().getId())
                    .title(document.getTitle())
                    .originalFilename(document.getOriginalFilename())
                    .fileSize(document.getFileSize())
                    .pageCount(document.getPageCount())
                    .grade(document.getGrade())
                    .subjectPath(document.getSubjectPath())
                    .processStatus(document.getProcessStatus())
                    .createdAt(document.getCreatedAt())
                    .updatedAt(document.getUpdatedAt())
                    .build())
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(
                StudentDocumentListResponseDto.builder()
                    .success(true)
                    .message("문서 목록 조회 성공")
                    .documents(documentDtos)
                    .build()
            );
            
        } catch (Exception e) {
            log.error("보호자 문서 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(500).body(
                StudentDocumentListResponseDto.builder()
                    .success(false)
                    .message("문서 목록 조회 중 오류가 발생했습니다: " + e.getMessage())
                    .build()
            );
        }
    }
    
    @Operation(summary = "학생에게 문서 할당", description = "보호자가 학생에게 문서를 할당합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "할당 성공", 
                     content = @Content(schema = @Schema(implementation = ResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "보호자, 학생 또는 문서를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/assign")
    public ResponseEntity<ResponseDto> assignDocumentToStudent(
            @RequestBody DocumentAssignmentRequestDto request) {
        
        log.info("문서 할당 요청: 보호자 ID: {}, 학생 ID: {}, 문서 ID: {}", 
                request.getGuardianId(), request.getStudentId(), request.getDocumentId());
        
        try {
            // 보호자 존재 여부 확인
            Guardian guardian = guardianRepository.findById(request.getGuardianId())
                .orElseThrow(() -> new IllegalArgumentException("보호자를 찾을 수 없습니다."));
            
            // 학생 존재 여부 확인
            Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));
            
            // 문서 존재 여부 확인
            Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다."));
            
            // 이미 할당되었는지 확인
            boolean alreadyAssigned = assignmentRepository.findByStudentIdAndDocumentId(
                    request.getStudentId(), request.getDocumentId()).isPresent();
            
            if (alreadyAssigned) {
                return ResponseEntity.ok(
                    ResponseDto.builder()
                        .success(true)
                        .message("이미 학생에게 할당된 문서입니다.")
                        .build()
                );
            }
            
            // 할당 정보 생성
            StudentDocumentAssignment assignment = StudentDocumentAssignment.builder()
                .student(student)
                .document(document)
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
                    .message("문서가 학생에게 할당되었습니다.")
                    .build()
            );
            
        } catch (IllegalArgumentException e) {
            log.error("문서 할당 중 오류 발생", e);
            return ResponseEntity.status(404).body(
                ResponseDto.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            log.error("문서 할당 중 오류 발생", e);
            return ResponseEntity.status(500).body(
                ResponseDto.builder()
                    .success(false)
                    .message("문서 할당 중 오류가 발생했습니다: " + e.getMessage())
                    .build()
            );
        }
    }
    
    @Operation(summary = "학생 문서 할당 취소", description = "보호자가 학생에게 할당한 문서를 취소합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "취소 성공", 
                     content = @Content(schema = @Schema(implementation = ResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "할당 정보를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/assign/{guardianId}/{studentId}/{documentId}")
    public ResponseEntity<ResponseDto> unassignDocumentFromStudent(
            @Parameter(description = "보호자 ID", required = true) 
            @PathVariable Long guardianId,
            
            @Parameter(description = "학생 ID", required = true) 
            @PathVariable Long studentId,
            
            @Parameter(description = "문서 ID", required = true) 
            @PathVariable Long documentId) {
        
        log.info("문서 할당 취소 요청: 보호자 ID: {}, 학생 ID: {}, 문서 ID: {}", 
                guardianId, studentId, documentId);
        
        try {
            // 할당 정보 조회
            StudentDocumentAssignment assignment = assignmentRepository
                .findByStudentIdAndDocumentId(studentId, documentId)
                .orElseThrow(() -> new IllegalArgumentException("할당된 문서를 찾을 수 없습니다."));
            
            // 할당한 보호자인지 확인
            if (!assignment.getGuardian().getId().equals(guardianId)) {
                return ResponseEntity.status(403).body(
                    ResponseDto.builder()
                        .success(false)
                        .message("이 문서를 할당한 보호자만 취소할 수 있습니다.")
                        .build()
                );
            }
            
            // 삭제
            assignmentRepository.delete(assignment);
            
            return ResponseEntity.ok(
                ResponseDto.builder()
                    .success(true)
                    .message("문서 할당이 취소되었습니다.")
                    .build()
            );
            
        } catch (IllegalArgumentException e) {
            log.error("문서 할당 취소 중 오류 발생", e);
            return ResponseEntity.status(404).body(
                ResponseDto.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            log.error("문서 할당 취소 중 오류 발생", e);
            return ResponseEntity.status(500).body(
                ResponseDto.builder()
                    .success(false)
                    .message("문서 할당 취소 중 오류가 발생했습니다: " + e.getMessage())
                    .build()
            );
        }
    }
    
    @Operation(summary = "보호자의 학생별 할당 문서 목록 조회", description = "보호자가 특정 학생에게 할당한 문서 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", 
                     content = @Content(schema = @Schema(implementation = StudentDocumentListResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "보호자 또는 학생을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{guardianId}/student/{studentId}")
    public ResponseEntity<StudentDocumentListResponseDto> getAssignedDocumentsForStudent(
            @Parameter(description = "보호자 ID", required = true) 
            @PathVariable Long guardianId,
            
            @Parameter(description = "학생 ID", required = true) 
            @PathVariable Long studentId) {
        
        log.info("보호자 ID: {}, 학생 ID: {}의 할당된 문서 목록 조회 요청", guardianId, studentId);
        
        try {
            List<StudentDocumentAssignment> assignments = assignmentRepository
                .findByAssignedByIdAndStudentId(guardianId, studentId);
            
            if (assignments.isEmpty()) {
                return ResponseEntity.ok(
                    StudentDocumentListResponseDto.builder()
                        .success(true)
                        .message("할당된 문서가 없습니다.")
                        .documents(List.of())
                        .build()
                );
            }
            
            List<DocumentDto> documents = assignments.stream()
                .map(assignment -> {
                    Document document = assignment.getDocument();
                    return DocumentDto.builder()
                        .id(document.getId())
                        .guardianId(document.getGuardian().getId())
                        .title(document.getTitle())
                        .originalFilename(document.getOriginalFilename())
                        .fileSize(document.getFileSize())
                        .pageCount(document.getPageCount())
                        .grade(document.getGrade())
                        .subjectPath(document.getSubjectPath())
                        .processStatus(document.getProcessStatus())
                        .createdAt(document.getCreatedAt())
                        .updatedAt(document.getUpdatedAt())
                        .build();
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(
                StudentDocumentListResponseDto.builder()
                    .success(true)
                    .message("할당된 문서 목록 조회 성공")
                    .documents(documents)
                    .build()
            );
            
        } catch (Exception e) {
            log.error("할당된 문서 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(500).body(
                StudentDocumentListResponseDto.builder()
                    .success(false)
                    .message("할당된 문서 목록 조회 중 오류가 발생했습니다: " + e.getMessage())
                    .build()
            );
        }
    }
} 