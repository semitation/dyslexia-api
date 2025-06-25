package com.dyslexia.dyslexia.mapper;

import com.dyslexia.dyslexia.dto.PageDto;
import com.dyslexia.dyslexia.entity.Page;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PageMapper {

  @Mapping(source = "textbook.id", target = "textbookId")
  PageDto toDto(Page page);
}
