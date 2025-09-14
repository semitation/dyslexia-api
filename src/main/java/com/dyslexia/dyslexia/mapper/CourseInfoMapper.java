package com.dyslexia.dyslexia.mapper;

import com.dyslexia.dyslexia.dto.CourseInfoDto;
import com.dyslexia.dyslexia.dto.CourseInfoReqDto;
import com.dyslexia.dyslexia.entity.CourseInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseInfoMapper {

  CourseInfoDto toDto(CourseInfo entity);

  CourseInfo toEntity(CourseInfoReqDto dto);
}