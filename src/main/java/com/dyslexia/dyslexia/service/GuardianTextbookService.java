package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.TextbookDto;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.entity.StudentTextbookAssignment;
import com.dyslexia.dyslexia.entity.Textbook;
import com.dyslexia.dyslexia.exception.ApplicationException;
import com.dyslexia.dyslexia.exception.ExceptionCode;
import com.dyslexia.dyslexia.mapper.TextbookMapper;
import com.dyslexia.dyslexia.repository.GuardianRepository;
import com.dyslexia.dyslexia.repository.StudentRepository;
import com.dyslexia.dyslexia.repository.StudentTextbookAssignmentRepository;
import com.dyslexia.dyslexia.repository.TextbookRepository;
import com.dyslexia.dyslexia.util.JwtTokenProvider;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GuardianTextbookService {

    private final GuardianRepository guardianRepository;
    private final TextbookRepository textbookRepository;
    private final StudentRepository studentRepository;
    private final StudentTextbookAssignmentRepository assignmentRepository;
    private final TextbookMapper textbookMapper;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 현재 인증된 보호자의 교재 목록 조회
     */
    public List<TextbookDto> getMyTextbooks() {
        String currentClientId = jwtTokenProvider.getCurrentClientId();
        Guardian guardian = guardianRepository.findByClientId(currentClientId)
            .orElseThrow(() -> new ApplicationException(ExceptionCode.GUARDIAN_NOT_FOUND));

        List<Textbook> textbooks = textbookRepository.findByGuardianIdOrderByUpdatedAtDesc(guardian.getId());
        
        return textbooks.stream()
            .map(textbookMapper::toDto)
            .toList();
    }

    /**
     * 현재 인증된 보호자가 학생에게 교재 할당
     */
    @Transactional
    public void assignTextbookToStudent(Long studentId, Long textbookId, String notes) {
        String currentClientId = jwtTokenProvider.getCurrentClientId();
        Guardian guardian = guardianRepository.findByClientId(currentClientId)
            .orElseThrow(() -> new ApplicationException(ExceptionCode.GUARDIAN_NOT_FOUND));

        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ApplicationException(ExceptionCode.STUDENT_NOT_FOUND));

        Textbook textbook = textbookRepository.findById(textbookId)
            .orElseThrow(() -> new ApplicationException(ExceptionCode.ENTITY_NOT_FOUND));

        // 이미 할당된 교재인지 확인
        assignmentRepository.findByStudentIdAndTextbookId(studentId, textbookId)
            .ifPresent(a -> {
                throw new ApplicationException(ExceptionCode.BAD_REQUEST_ERROR);
            });

        // 할당 정보 생성
        StudentTextbookAssignment assignment = StudentTextbookAssignment.builder()
            .student(student)
            .textbook(textbook)
            .assignedBy(guardian)
            .assignedAt(LocalDateTime.now())
            .notes(notes)
            .build();

        assignmentRepository.save(assignment);
    }

    /**
     * 현재 인증된 보호자가 학생의 교재 할당 취소
     */
    @Transactional
    public void unassignTextbookFromStudent(Long studentId, Long textbookId) {
        String currentClientId = jwtTokenProvider.getCurrentClientId();
        Guardian guardian = guardianRepository.findByClientId(currentClientId)
            .orElseThrow(() -> new ApplicationException(ExceptionCode.GUARDIAN_NOT_FOUND));

        StudentTextbookAssignment assignment = assignmentRepository
            .findByStudentIdAndTextbookId(studentId, textbookId)
            .orElseThrow(() -> new ApplicationException(ExceptionCode.ENTITY_NOT_FOUND));

        // 본인이 할당한 교재만 취소 가능
        if (!assignment.getAssignedBy().getId().equals(guardian.getId())) {
            throw new ApplicationException(ExceptionCode.ACCESS_DENIED);
        }

        assignmentRepository.delete(assignment);
    }

    /**
     * 현재 인증된 보호자가 특정 학생에게 할당한 교재 목록 조회
     */
    public List<TextbookDto> getAssignedTextbooksForStudent(Long studentId) {
        String currentClientId = jwtTokenProvider.getCurrentClientId();
        Guardian guardian = guardianRepository.findByClientId(currentClientId)
            .orElseThrow(() -> new ApplicationException(ExceptionCode.GUARDIAN_NOT_FOUND));

        List<StudentTextbookAssignment> assignments =
            assignmentRepository.findByAssignedByIdAndStudentId(guardian.getId(), studentId);

        return assignments.stream()
            .map(StudentTextbookAssignment::getTextbook)
            .map(textbookMapper::toDto)
            .toList();
    }
}
