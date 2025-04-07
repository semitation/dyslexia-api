package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.TeacherCodeDto;
import com.dyslexia.dyslexia.dto.TeacherDto;
import com.dyslexia.dyslexia.entity.Teacher;
import com.dyslexia.dyslexia.mapper.TeacherMapper;
import com.dyslexia.dyslexia.repository.TeacherRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeacherService {

  private final TeacherRepository teacherRepository;
  private final TeacherMapper teacherMapper;

  @Transactional
  public TeacherDto saveTeacher(TeacherDto dto) {

    Teacher teacher = teacherRepository.save(teacherMapper.toEntity(dto));
    teacher.generateMatchCode();

    while (teacherRepository.existsByMatchCodeAndIdNot(teacher.getMatchCode(), teacher.getId())) {
      teacher.generateMatchCode();
    }

    return teacherMapper.toDto(teacher);
  }

  public TeacherDto getById(Long id) throws NotFoundException {
    return teacherRepository.findById(id).map(teacherMapper::toDto)
        .orElseThrow(NotFoundException::new);
  }

  public Optional<TeacherDto> getTeacherByClientId(String clientId) {
    return teacherRepository.findByClientId(clientId).map(teacherMapper::toDto);
  }

  public TeacherCodeDto getCodeById(long id) throws NotFoundException {
    Teacher teacher = teacherRepository.findById(id).orElseThrow(NotFoundException::new);

    return teacherMapper.toCodeDto(teacher);
  }

}
