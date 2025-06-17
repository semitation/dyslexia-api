package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.SignUpRequestDto;
import com.dyslexia.dyslexia.dto.SignUpResponseDto;
import com.dyslexia.dyslexia.dto.UserInfoDto;
import com.dyslexia.dyslexia.dto.StudentInfoDto;
import com.dyslexia.dyslexia.dto.GuardianInfoDto;
import com.dyslexia.dyslexia.entity.Interest;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.enums.UserType;
import com.dyslexia.dyslexia.exception.UserAlreadyExistsException;
import com.dyslexia.dyslexia.mapper.StudentMapper;
import com.dyslexia.dyslexia.mapper.GuardianMapper;
import com.dyslexia.dyslexia.repository.InterestRepository;
import com.dyslexia.dyslexia.repository.StudentRepository;
import com.dyslexia.dyslexia.repository.GuardianRepository;
import com.dyslexia.dyslexia.util.JwtTokenProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final GuardianRepository guardianRepository;
    private final StudentRepository studentRepository;
    private final InterestRepository interestRepository;
    private final GuardianMapper guardianMapper;
    private final StudentMapper studentMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto dto) {

        if (UserType.GUARDIAN == dto.getUserType()) {
            return registerGuardian(dto);
        } else {
            return registerStudent(dto);
        }
    }

    private SignUpResponseDto registerGuardian(SignUpRequestDto dto) {
        if (guardianRepository.existsByClientId(dto.getClientId())) {
            throw new UserAlreadyExistsException("이미 가입된 사용자입니다.");
        }

        Guardian guardian = guardianRepository.save(guardianMapper.toEntity(dto));

        guardian.generateMatchCode();

        while (guardianRepository.existsByMatchCodeAndIdNot(guardian.getMatchCode(),
            guardian.getId())) {
            guardian.generateMatchCode();
        }

        String accessToken = jwtTokenProvider.createAccessToken(guardian.getClientId(),
            UserType.GUARDIAN.name());
        String refreshToken = jwtTokenProvider.createRefreshToken(guardian.getClientId());

        return SignUpResponseDto.builder().id(guardian.getId()).name(guardian.getName())
            .userType(UserType.GUARDIAN).accessToken(accessToken).refreshToken(refreshToken).build();
    }

    private SignUpResponseDto registerStudent(SignUpRequestDto dto) {
        if (studentRepository.existsByClientId(dto.getClientId())) {
            throw new UserAlreadyExistsException("이미 가입된 사용자입니다.");
        }

        Student student = studentMapper.toEntity(dto);

        if (dto.getInterestIds() != null && !dto.getInterestIds().isEmpty()) {

            List<Interest> interests = interestRepository.findAllById(dto.getInterestIds());

            student.addInterests(interests);
        }

        student = studentRepository.save(student);

        String accessToken = jwtTokenProvider.createAccessToken(student.getClientId(),
            UserType.STUDENT.name());
        String refreshToken = jwtTokenProvider.createRefreshToken(student.getClientId());

        return SignUpResponseDto.builder().id(student.getId()).name(student.getName())
            .userType(UserType.STUDENT).accessToken(accessToken).refreshToken(refreshToken).build();
    }

    @Transactional(readOnly = true)
    public UserInfoDto getMyInfo(String clientId, UserType userType) {
        return switch (userType) {
            case STUDENT -> {
                Student student = studentRepository.findByClientId(clientId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));
                yield new StudentInfoDto(student);
            }
            case GUARDIAN -> {
                Guardian guardian = guardianRepository.findByClientId(clientId)
                    .orElseThrow(() -> new RuntimeException("Guardian not found"));
                yield new GuardianInfoDto(guardian);
            }
            default -> throw new RuntimeException("Invalid user type");
        };
    }
}