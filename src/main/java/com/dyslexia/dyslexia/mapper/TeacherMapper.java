package com.dyslexia.dyslexia.mapper;

import com.dyslexia.dyslexia.dto.TeacherDto;
import com.dyslexia.dyslexia.dto.TeacherInfoDto;
import com.dyslexia.dyslexia.dto.TeacherCodeDto;
import com.dyslexia.dyslexia.entity.Teacher;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TeacherMapper {

  TeacherDto toDto(Teacher entity);

  TeacherInfoDto toInfoDto(Teacher entity);

  TeacherCodeDto toCodeDto(Teacher entity);

  Teacher toEntity(TeacherDto dto);
}