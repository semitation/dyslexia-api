package com.dyslexia.dyslexia.dto;

import com.dyslexia.dyslexia.enums.Grade;
import com.dyslexia.dyslexia.enums.UserType;
import java.util.List;

public record StudentSignUpRequestDto(String clientId, String name, Grade grade, List<Long> interests) {}
