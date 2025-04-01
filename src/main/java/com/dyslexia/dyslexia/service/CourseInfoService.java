package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.CourseInfoDto;
import com.dyslexia.dyslexia.dto.CourseInfoReqDto;
import com.dyslexia.dyslexia.entity.Course;
import com.dyslexia.dyslexia.entity.CourseInfo;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.entity.Teacher;
import com.dyslexia.dyslexia.mapper.CourseInfoMapper;
import com.dyslexia.dyslexia.repository.CourseInfoRepository;
import com.dyslexia.dyslexia.repository.CourseRepository;
import com.dyslexia.dyslexia.repository.StudentRepository;
import com.dyslexia.dyslexia.repository.TeacherRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseInfoService {

  private final CourseInfoRepository courseInfoRepository;
  private final CourseInfoMapper courseInfoMapper;
  private final CourseRepository courseRepository;
  private final StudentRepository studentRepository;
  private final TeacherRepository teacherRepository;

  public CourseInfoDto saveCourseInfo(CourseInfoReqDto dto) {

    Course course = courseRepository.findById(dto.getCourseId())
        .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    Student student = studentRepository.findById(dto.getStudentId())
        .orElseThrow(() -> new IllegalArgumentException("Student not found"));
    Teacher teacher = teacherRepository.findById(dto.getTeacherId())
        .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

    CourseInfo info = CourseInfo.builder().course(course).student(student).teacher(teacher)
        .learningTime(dto.getLearningTime()).page(dto.getPage()).maxPage(dto.getMaxPage()).build();

    return courseInfoMapper.toDto(courseInfoRepository.save(info));
  }

  public List<CourseInfoDto> getInfosByStudent(Long studentId) {
    return courseInfoRepository.findByStudentId(studentId).stream().map(courseInfoMapper::toDto)
        .toList();
  }
}
