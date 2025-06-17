package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.GuardianCodeDto;
import com.dyslexia.dyslexia.dto.GuardianDto;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.exception.notfound.GuardianNotFoundException;
import com.dyslexia.dyslexia.mapper.GuardianMapper;
import com.dyslexia.dyslexia.repository.GuardianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
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
        .orElseThrow(() -> new GuardianNotFoundException("아이디 '" + id + "'에 해당하는 보호자를 찾을 수 없습니다."));

    return guardianMapper.toDto(guardian);
  }

  public GuardianDto getGuardianByClientId(String clientId) {
    Guardian guardian = guardianRepository.findByClientId(clientId)
        .orElseThrow(() -> new GuardianNotFoundException("클라이언트 '" + clientId + "'에 해당하는 보호자를 찾을 수 없습니다."));

    return guardianMapper.toDto(guardian);

  }

  @Transactional(readOnly = true)
  public GuardianCodeDto getCodeById(long id) throws NotFoundException {
    Guardian guardian = guardianRepository.findById(id).orElseThrow(NotFoundException::new);

    return guardianMapper.toCodeDto(guardian);
  }

}
