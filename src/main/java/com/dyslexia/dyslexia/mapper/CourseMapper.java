package com.dyslexia.dyslexia.mapper;

import com.dyslexia.dyslexia.dto.CourseDto;
import com.dyslexia.dyslexia.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseMapper {
  @Mapping(source = "teacherId", target = "teacher.id")
  Course toEntity(CourseDto dto);

  @Mapping(source = "teacher.id", target = "teacherId")
  CourseDto toDto(Course entity);
}
