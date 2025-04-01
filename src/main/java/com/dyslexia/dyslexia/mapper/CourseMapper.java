package com.dyslexia.dyslexia.mapper;

import com.dyslexia.dyslexia.dto.CourseDto;
import com.dyslexia.dyslexia.entity.Course;
import com.dyslexia.dyslexia.mapper.custom.GradeMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {GradeMapper.class})
public interface CourseMapper {

  @Mapping(source = "teacherId", target = "teacher.id")
  @Mapping(source = "grade", target = "grade")
  Course toEntity(CourseDto dto);

  @Mapping(source = "teacher.id", target = "teacherId")
  @Mapping(source = "grade", target = "grade")
  CourseDto toDto(Course entity);
}
