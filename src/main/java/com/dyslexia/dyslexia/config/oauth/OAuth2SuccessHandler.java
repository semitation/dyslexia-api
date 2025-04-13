package com.dyslexia.dyslexia.config.oauth;

import com.dyslexia.dyslexia.dto.AuthResponseDto;
import com.dyslexia.dyslexia.enums.UserType;
import com.dyslexia.dyslexia.repository.StudentRepository;
import com.dyslexia.dyslexia.repository.TeacherRepository;
import com.dyslexia.dyslexia.util.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    @Override
    @SuppressWarnings("unchecked")
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

        String clientId = attributes.get("id").toString();
        String nickname = (String) kakaoProfile.get("nickname");

        UserType userType = determineUserType(clientId);

        AuthResponseDto responseDto;

        if (userType != UserType.UNREGISTERED) {
            String accessToken = jwtTokenProvider.createAccessToken(clientId, userType.name());
            String refreshToken = jwtTokenProvider.createRefreshToken(clientId);

            responseDto = AuthResponseDto.builder().registered(true).clientId(clientId)
                .nickname(nickname).userType(userType.name()).accessToken(accessToken)
                .refreshToken(refreshToken).build();
        } else {
            responseDto = AuthResponseDto.builder().registered(false).clientId(clientId)
                .nickname(nickname).userType(UserType.UNREGISTERED.name()).build();
        }

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
    }

    private UserType determineUserType(String clientId) {
        if (studentRepository.findByClientId(clientId).isPresent()) {
            return UserType.STUDENT;
        }
        if (teacherRepository.findByClientId(clientId).isPresent()) {
            return UserType.TEACHER;
        }
        return UserType.UNREGISTERED;
    }
}