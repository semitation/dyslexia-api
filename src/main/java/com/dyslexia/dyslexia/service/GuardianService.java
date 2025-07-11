package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.GuardianCodeDto;
import com.dyslexia.dyslexia.dto.GuardianDto;
import com.dyslexia.dyslexia.dto.StudentDto;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.exception.ApplicationException;
import com.dyslexia.dyslexia.exception.ExceptionCode;
import com.dyslexia.dyslexia.mapper.GuardianMapper;
import com.dyslexia.dyslexia.mapper.StudentMapper;
import com.dyslexia.dyslexia.repository.GuardianRepository;
import com.dyslexia.dyslexia.util.JwtTokenProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuardianService {

  private final GuardianRepository guardianRepository;
  private final GuardianMapper guardianMapper;
  private final StudentMapper studentMapper;
  private final JwtTokenProvider jwtTokenProvider;

  public GuardianDto getById(Long id) {
    Guardian guardian = guardianRepository.findById(id)
        .orElseThrow(() -> new ApplicationException(ExceptionCode.GUARDIAN_NOT_FOUND));

    return guardianMapper.toDto(guardian);
  }

  public GuardianDto getGuardianByClientId(String clientId) {
    Guardian guardian = guardianRepository.findByClientId(clientId)
        .orElseThrow(() -> new ApplicationException(ExceptionCode.GUARDIAN_NOT_FOUND));

    return guardianMapper.toDto(guardian);
  }

  @Transactional(readOnly = true)
  public GuardianCodeDto getCodeById(long id) {
    Guardian guardian = guardianRepository.findById(id)
        .orElseThrow(() -> new ApplicationException(ExceptionCode.GUARDIAN_NOT_FOUND));

    return guardianMapper.toCodeDto(guardian);
  }

  /**
   * 현재 인증된 보호자 정보 조회
   */
  public GuardianDto getMyInfo() {
    String currentClientId = jwtTokenProvider.getCurrentClientId();
    Guardian guardian = guardianRepository.findByClientId(currentClientId)
        .orElseThrow(() -> new ApplicationException(ExceptionCode.GUARDIAN_NOT_FOUND));

    return guardianMapper.toDto(guardian);
  }

  /**
   * 현재 인증된 보호자의 매칭 코드 조회
   */
  public GuardianCodeDto getMyCode() {
    String currentClientId = jwtTokenProvider.getCurrentClientId();
    Guardian guardian = guardianRepository.findByClientId(currentClientId)
        .orElseThrow(() -> new ApplicationException(ExceptionCode.GUARDIAN_NOT_FOUND));

    return guardianMapper.toCodeDto(guardian);
  }

  /**
   * 현재 인증된 보호자의 담당 학생 목록 조회
   */
  public List<StudentDto> getMyStudents() {
    String currentClientId = jwtTokenProvider.getCurrentClientId();
    Guardian guardian = guardianRepository.findByClientId(currentClientId)
        .orElseThrow(() -> new ApplicationException(ExceptionCode.GUARDIAN_NOT_FOUND));

    return guardian.getStudents().stream()
        .map(studentMapper::toDto)
        .toList();
  }
}
