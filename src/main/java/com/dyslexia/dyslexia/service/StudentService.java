package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.MatchResponseDto;
import com.dyslexia.dyslexia.dto.StudentDto;
import com.dyslexia.dyslexia.dto.GuardianDto;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.exception.notfound.StudentNotFoundException;
import com.dyslexia.dyslexia.exception.notfound.GuardianNotFoundException;
import com.dyslexia.dyslexia.mapper.StudentMapper;
import com.dyslexia.dyslexia.mapper.GuardianMapper;
import com.dyslexia.dyslexia.repository.StudentRepository;
import com.dyslexia.dyslexia.repository.GuardianRepository;
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

  public StudentDto getById(Long id) {
    Student student = studentRepository.findById(id)
        .orElseThrow(() -> new StudentNotFoundException("아이디 '" + id + "'에 해당하는 학생을 찾을 수 없습니다."));

    return studentMapper.toDto(student);
  }

  public List<StudentDto> getStudentsByGuardian(Long guardianId) {
    return studentRepository.findByGuardianId(guardianId).stream().map(studentMapper::toDto).toList();
  }

  @Transactional
  public MatchResponseDto matchByCode(Long id, String code) {
    Guardian guardian = guardianRepository.findByMatchCode(code)
        .orElseThrow(() -> new GuardianNotFoundException("코드 '" + code + "'에 해당하는 보호자를 찾을 수 없습니다."));

    Student student = studentRepository.findById(id)
        .orElseThrow(() -> new StudentNotFoundException("학생을 찾을 수 없습니다."));

    guardian.addStudent(student);
    return guardianMapper.toMatchResponseDto(guardian);
  }

  public StudentDto getByClientId(String clientId) {
    Student student = studentRepository.findByClientId(clientId)
        .orElseThrow(() -> new GuardianNotFoundException("클라이언트 '" + clientId + "'에 해당하는 학생을 찾을 수 없습니다."));

    return studentMapper.toDto(student);
  }

}