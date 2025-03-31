package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.StudentDto;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.entity.Teacher;
import com.dyslexia.dyslexia.mapper.StudentMapper;
import com.dyslexia.dyslexia.repository.StudentRepository;
import com.dyslexia.dyslexia.repository.TeacherRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

  private final StudentRepository studentRepository;
  private final StudentMapper studentMapper;
  private final TeacherRepository teacherRepository;

  public StudentDto saveStudent(StudentDto dto) {
    Teacher teacher = teacherRepository.findById(dto.getTeacherId())
        .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
    Student student = studentMapper.toEntity(dto);
    student.setTeacher(teacher);
    return studentMapper.toDto(studentRepository.save(student));
  }

  public List<StudentDto> getStudentsByTeacher(Long teacherId) {
    return studentRepository.findByTeacherId(teacherId)
        .stream()
        .map(studentMapper::toDto)
        .toList();
  }
}