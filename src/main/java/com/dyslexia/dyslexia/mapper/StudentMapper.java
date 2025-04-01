package com.dyslexia.dyslexia.mapper;

import com.dyslexia.dyslexia.dto.StudentDto;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.mapper.custom.GradeMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {GradeMapper.class})
public interface StudentMapper {

  @Mapping(source = "teacherId", target = "teacher.id")
  @Mapping(source = "grade", target = "grade")
  Student toEntity(StudentDto dto);

  @Mapping(source = "teacher.id", target = "teacherId")
  @Mapping(source = "grade", target = "grade")
  StudentDto toDto(Student entity);
}
