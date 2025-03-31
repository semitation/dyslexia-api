package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.TeacherDto;
import com.dyslexia.dyslexia.mapper.TeacherMapper;
import com.dyslexia.dyslexia.repository.TeacherRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeacherService {

  private final TeacherRepository teacherRepository;
  private final TeacherMapper teacherMapper;

  public TeacherDto saveTeacher(TeacherDto dto) {
    return teacherMapper.toDto(teacherRepository.save(teacherMapper.toEntity(dto)));
  }

  public Optional<TeacherDto> getTeacherByClientId(String clientId) {
    return teacherRepository.findByClientId(clientId).map(teacherMapper::toDto);
  }
}
