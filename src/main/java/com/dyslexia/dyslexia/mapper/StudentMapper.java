package com.dyslexia.dyslexia.mapper;

import com.dyslexia.dyslexia.dto.StudentDto;
import com.dyslexia.dyslexia.dto.StudentSignUpRequestDto;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.mapper.custom.GradeMapper;
import com.dyslexia.dyslexia.mapper.custom.InterestMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {GradeMapper.class, InterestMapper.class})
public interface StudentMapper {

  @Mapping(source = "name", target = "name")
  @Mapping(source = "guardian.id", target = "guardianId")
  @Mapping(source = "grade", target = "grade")
  @Mapping(source = "interests", target = "interests")
  StudentDto toDto(Student entity);

  @Mapping(target = "guardian", ignore = true)
  @Mapping(target = "type", ignore = true)
  @Mapping(target = "state", ignore = true)
  @Mapping(target = "profileImageUrl", ignore = true)
  @Mapping(target = "interests", ignore = true)
  @Mapping(target = "defaultFontSize", ignore = true)
  @Mapping(target = "defaultLineSpacing", ignore = true)
  @Mapping(target = "defaultLetterSpacing", ignore = true)
  @Mapping(target = "defaultColorScheme", ignore = true)
  @Mapping(target = "defaultTextToSpeechEnabled", ignore = true)
  @Mapping(target = "defaultReadingHighlightEnabled", ignore = true)
  @Mapping(target = "defaultBackgroundColor", ignore = true)
  @Mapping(target = "defaultTextColor", ignore = true)
  Student toEntity(StudentSignUpRequestDto dto);

}
