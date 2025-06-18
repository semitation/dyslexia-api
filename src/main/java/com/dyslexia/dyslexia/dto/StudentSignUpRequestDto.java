package com.dyslexia.dyslexia.dto;

import com.dyslexia.dyslexia.enums.Grade;
import com.dyslexia.dyslexia.enums.UserType;
import java.util.List;
import lombok.Builder;

@Builder
public record StudentSignUpRequestDto(String clientId, String name, Grade grade, List<Long> interests) {}
