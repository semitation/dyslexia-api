package com.dyslexia.dyslexia.mapper;

import com.dyslexia.dyslexia.dto.CourseDto;
import com.dyslexia.dyslexia.dto.CourseReqDto;
import com.dyslexia.dyslexia.entity.Course;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseMapper {

  CourseDto toDto(Course entity);

  Course toEntity(CourseReqDto dto);
}