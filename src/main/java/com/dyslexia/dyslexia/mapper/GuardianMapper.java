package com.dyslexia.dyslexia.mapper;

import com.dyslexia.dyslexia.dto.GuardianSignUpRequestDto;
import com.dyslexia.dyslexia.dto.MatchResponseDto;
import com.dyslexia.dyslexia.dto.GuardianCodeDto;
import com.dyslexia.dyslexia.dto.GuardianDto;
import com.dyslexia.dyslexia.entity.Guardian;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GuardianMapper {

  Guardian toEntity(GuardianDto dto);

  @Mapping(target = "profileImageUrl", ignore = true)
  Guardian toEntity(GuardianSignUpRequestDto dto);

  GuardianDto toDto(Guardian entity);

  @Mapping(target = "matchCode", source = "matchCode")
  GuardianCodeDto toCodeDto(Guardian guardian);

  MatchResponseDto toMatchResponseDto(Guardian guardian);
}