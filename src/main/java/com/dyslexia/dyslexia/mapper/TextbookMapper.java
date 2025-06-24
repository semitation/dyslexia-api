package com.dyslexia.dyslexia.mapper;

import com.dyslexia.dyslexia.dto.TextbookDto;
import com.dyslexia.dyslexia.entity.Textbook;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TextbookMapper {

  @Mapping(source = "guardian.id", target = "guardianId")
  TextbookDto toDto(Textbook textbook);
}