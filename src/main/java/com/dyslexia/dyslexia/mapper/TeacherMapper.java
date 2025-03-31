package com.dyslexia.dyslexia.mapper;
import com.dyslexia.dyslexia.dto.TeacherDto;
import com.dyslexia.dyslexia.entity.Teacher;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TeacherMapper {
  Teacher toEntity(TeacherDto dto);
  TeacherDto toDto(Teacher entity);
}
