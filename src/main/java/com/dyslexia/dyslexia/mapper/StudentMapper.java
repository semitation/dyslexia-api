package com.dyslexia.dyslexia.mapper;

import com.dyslexia.dyslexia.dto.StudentDto;
import com.dyslexia.dyslexia.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentMapper {
  Student toEntity(StudentDto dto);

  @Mapping(source = "teacher.id", target = "teacherId")
  StudentDto toDto(Student entity);
}
