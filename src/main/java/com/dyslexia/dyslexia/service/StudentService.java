package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.MatchResponseDto;
import com.dyslexia.dyslexia.dto.StudentDto;
import com.dyslexia.dyslexia.dto.GuardianDto;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.exception.ApplicationException;
import com.dyslexia.dyslexia.exception.ExceptionCode;
import com.dyslexia.dyslexia.mapper.StudentMapper;
import com.dyslexia.dyslexia.mapper.GuardianMapper;
import com.dyslexia.dyslexia.repository.StudentRepository;
import com.dyslexia.dyslexia.repository.GuardianRepository;
import com.dyslexia.dyslexia.util.JwtTokenProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

  private final StudentRepository studentRepository;
  private final GuardianRepository guardianRepository;
  private final StudentMapper studentMapper;
  private final GuardianMapper guardianMapper;
  private final JwtTokenProvider jwtTokenProvider;

  public StudentDto getById(Long id) {
    Student student = studentRepository.findById(id)
        .orElseThrow(() -> new ApplicationException(ExceptionCode.STUDENT_NOT_FOUND));

    return studentMapper.toDto(student);
  }

  public List<StudentDto> getStudentsByGuardian(Long guardianId) {
    return studentRepository.findByGuardianId(guardianId).stream().map(studentMapper::toDto).toList();
  }

  @Transactional
  public MatchResponseDto matchByCode(Long id, String code) {
    Guardian guardian = guardianRepository.findByMatchCode(code)
        .orElseThrow(() -> new ApplicationException(ExceptionCode.GUARDIAN_NOT_FOUND));

    Student student = studentRepository.findById(id)
        .orElseThrow(() -> new ApplicationException(ExceptionCode.STUDENT_NOT_FOUND));

    guardian.addStudent(student);
    return guardianMapper.toMatchResponseDto(guardian);
  }

  public StudentDto getByClientId(String clientId) {
    Student student = studentRepository.findByClientId(clientId)
        .orElseThrow(() -> new ApplicationException(ExceptionCode.STUDENT_NOT_FOUND));

    return studentMapper.toDto(student);
  }

  /**
   * 현재 인증된 학생 정보 조회
   */
  public StudentDto getMyInfo() {
    String currentClientId = jwtTokenProvider.getCurrentClientId();
    Student student = studentRepository.findByClientId(currentClientId)
        .orElseThrow(() -> new ApplicationException(ExceptionCode.STUDENT_NOT_FOUND));

    return studentMapper.toDto(student);
  }

  /**
   * 현재 인증된 학생이 보호자와 매칭
   */
  @Transactional
  public MatchResponseDto matchWithGuardian(String code) {
    String currentClientId = jwtTokenProvider.getCurrentClientId();
    Student student = studentRepository.findByClientId(currentClientId)
        .orElseThrow(() -> new ApplicationException(ExceptionCode.STUDENT_NOT_FOUND));

    Guardian guardian = guardianRepository.findByMatchCode(code)
        .orElseThrow(() -> new ApplicationException(ExceptionCode.GUARDIAN_NOT_FOUND));

    guardian.addStudent(student);
    return guardianMapper.toMatchResponseDto(guardian);
  }

  /**
   * 현재 인증된 학생의 보호자 정보 조회
   */
  public GuardianDto getMyGuardian() {
    String currentClientId = jwtTokenProvider.getCurrentClientId();
    Student student = studentRepository.findByClientId(currentClientId)
        .orElseThrow(() -> new ApplicationException(ExceptionCode.STUDENT_NOT_FOUND));

    if (student.getGuardian() == null) {
      throw new ApplicationException(ExceptionCode.GUARDIAN_NOT_FOUND);
    }

    return guardianMapper.toDto(student.getGuardian());
  }
}
