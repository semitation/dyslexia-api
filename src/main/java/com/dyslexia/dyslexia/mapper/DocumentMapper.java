package com.dyslexia.dyslexia.mapper;

import com.dyslexia.dyslexia.dto.DocumentDto;
import com.dyslexia.dyslexia.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

  @Mapping(target = "guardianId", source = "guardian.id")
  DocumentDto toDto(Document document);
}
