package com.dyslexia.dyslexia.mapper;

import com.dyslexia.dyslexia.dto.StudentDto;
import com.dyslexia.dyslexia.entity.Interest;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.mapper.custom.GradeMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {GradeMapper.class})
public interface StudentMapper {

  @Mapping(source = "teacherId", target = "teacher.id")
  @Mapping(source = "grade", target = "grade")
  Student toEntity(StudentDto dto);

  @Mapping(source = "teacher.id", target = "teacherId")
  @Mapping(source = "grade", target = "grade")
  @Mapping(target = "interests", expression = "java(mapInterests(student.getInterests()))")
  StudentDto toDto(Student entity);

  default List<String> mapInterests(List<Interest> interests) {
    if (interests == null) {
      return new ArrayList<>();
    }
    return interests.stream().map(Interest::getName).collect(Collectors.toList());
  }
}
