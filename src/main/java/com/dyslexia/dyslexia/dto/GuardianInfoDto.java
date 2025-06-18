package com.dyslexia.dyslexia.dto;

import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.enums.UserType;
import lombok.Getter;

@Getter
public class GuardianInfoDto implements UserInfoDto {
    private final Long id;
    private final String clientId;
    private final String name;
    private final UserType userType;

    public GuardianInfoDto(Guardian guardian) {
        this.id = guardian.getId();
        this.clientId = guardian.getClientId();
        this.name = guardian.getName();
        this.userType = UserType.GUARDIAN;
    }
} 