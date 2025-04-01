package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.StudentDto;
import com.dyslexia.dyslexia.dto.StudentReqDto;
import com.dyslexia.dyslexia.entity.Interest;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.entity.Teacher;
import com.dyslexia.dyslexia.enums.Grade;
import com.dyslexia.dyslexia.mapper.StudentMapper;
import com.dyslexia.dyslexia.repository.InterestRepository;
import com.dyslexia.dyslexia.repository.StudentRepository;
import com.dyslexia.dyslexia.repository.TeacherRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

  private final StudentRepository studentRepository;
  private final StudentMapper studentMapper;
  private final TeacherRepository teacherRepository;

  private final InterestRepository interestRepository;

  public StudentDto saveStudent(StudentReqDto dto) {
    Teacher teacher = teacherRepository.findById(dto.getTeacherId())
        .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

    Grade grade = Grade.fromLabel(dto.getGradeLabel());

    List<Interest> interests = dto.getInterests().stream().map(
            name -> interestRepository.findByName(name)
                .orElseGet(() -> interestRepository.save(Interest.builder().name(name).build())))
        .toList();

    Student student = Student.builder().clientId(dto.getClientId()).teacher(teacher).grade(grade)
        .type(dto.getType()).state(dto.getState()).profileImageUrl(dto.getProfileImageUrl())
        .interests(interests).build();

    return studentMapper.toDto(studentRepository.save(student));
  }


  public StudentDto getById(Long id) throws NotFoundException {
    return studentRepository.findById(id).map(studentMapper::toDto)
        .orElseThrow(NotFoundException::new);
  }

  public List<StudentDto> getStudentsByTeacher(Long teacherId) {
    return studentRepository.findByTeacherId(teacherId).stream().map(studentMapper::toDto).toList();
  }
}