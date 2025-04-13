package com.dyslexia.dyslexia.config.oauth;

import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.entity.Teacher;
import com.dyslexia.dyslexia.repository.StudentRepository;
import com.dyslexia.dyslexia.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
            .getUserInfoEndpoint().getUserNameAttributeName();

        OAuth2Attributes attributes = OAuth2Attributes.of(registrationId, userNameAttributeName,
            oAuth2User.getAttributes());

        String clientId = attributes.getId();

        Optional<Student> student = studentRepository.findByClientId(clientId);
        Optional<Teacher> teacher = teacherRepository.findByClientId(clientId);

        String role = "ROLE_UNREGISTERED";
        if (student.isPresent()) {
            role = "ROLE_STUDENT";
        } else if (teacher.isPresent()) {
            role = "ROLE_TEACHER";
        }

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(role)),
            attributes.getAttributes(), attributes.getNameAttributeKey());
    }
}