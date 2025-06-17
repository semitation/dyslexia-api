package com.dyslexia.dyslexia.dto;

import com.dyslexia.dyslexia.enums.Grade;
import com.dyslexia.dyslexia.enums.GuardianRole;
import com.dyslexia.dyslexia.enums.UserType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
public record GuardianSignUpRequestDto(String clientId, String name, String email, GuardianRole guardianRole, String organization) {}