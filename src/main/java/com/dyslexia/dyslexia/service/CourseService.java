package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.CourseDto;
import com.dyslexia.dyslexia.entity.Course;
import com.dyslexia.dyslexia.entity.Teacher;
import com.dyslexia.dyslexia.mapper.CourseMapper;
import com.dyslexia.dyslexia.repository.CourseRepository;
import com.dyslexia.dyslexia.repository.TeacherRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseService {

  private final CourseRepository courseRepository;
  private final CourseMapper courseMapper;
  private final TeacherRepository teacherRepository;

  public CourseDto saveCourse(CourseDto dto) {
    Teacher teacher = teacherRepository.findById(dto.getTeacherId())
        .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
    Course course = courseMapper.toEntity(dto);
    course.setTeacher(teacher);
    return courseMapper.toDto(courseRepository.save(course));
  }

  public List<CourseDto> getCoursesByTeacher(Long teacherId) {
    return courseRepository.findByTeacherId(teacherId)
        .stream()
        .map(courseMapper::toDto)
        .toList();
  }
}
