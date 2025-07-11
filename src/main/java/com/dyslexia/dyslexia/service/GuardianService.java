package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.GuardianCodeDto;
import com.dyslexia.dyslexia.dto.GuardianDto;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.exception.ApplicationException;
import com.dyslexia.dyslexia.exception.ExceptionCode;
import com.dyslexia.dyslexia.mapper.GuardianMapper;
import com.dyslexia.dyslexia.repository.GuardianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuardianService {

  private final GuardianRepository guardianRepository;
  private final GuardianMapper guardianMapper;

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

}
