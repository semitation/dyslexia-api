package com.dyslexia.dyslexia.mapper;

import com.dyslexia.dyslexia.dto.CourseDto;
import com.dyslexia.dyslexia.entity.Course;
import com.dyslexia.dyslexia.mapper.custom.GradeMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {GradeMapper.class})
public interface CourseMapper {

  @Mapping(source = "guardian.id", target = "guardianId")
  @Mapping(source = "grade", target = "grade")
  CourseDto toDto(Course entity);
}
