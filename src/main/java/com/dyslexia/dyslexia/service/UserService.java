package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.GuardianSignUpRequestDto;
import com.dyslexia.dyslexia.dto.SignUpResponseDto;
import com.dyslexia.dyslexia.dto.StudentSignUpRequestDto;
import com.dyslexia.dyslexia.dto.UserInfoDto;
import com.dyslexia.dyslexia.dto.StudentInfoDto;
import com.dyslexia.dyslexia.dto.GuardianInfoDto;
import com.dyslexia.dyslexia.entity.Interest;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.enums.UserType;
import com.dyslexia.dyslexia.exception.UserAlreadyExistsException;
import com.dyslexia.dyslexia.exception.notfound.GuardianNotFoundException;
import com.dyslexia.dyslexia.exception.notfound.StudentNotFoundException;
import com.dyslexia.dyslexia.mapper.StudentMapper;
import com.dyslexia.dyslexia.mapper.GuardianMapper;
import com.dyslexia.dyslexia.repository.InterestRepository;
import com.dyslexia.dyslexia.repository.StudentRepository;
import com.dyslexia.dyslexia.repository.GuardianRepository;
import com.dyslexia.dyslexia.util.JwtTokenProvider;
import java.util.List;
import java.util.Optional;
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
    public SignUpResponseDto registerGuardian(GuardianSignUpRequestDto dto) {
        if (guardianRepository.existsByClientId(dto.clientId())) {
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

    @Transactional
    public SignUpResponseDto registerStudent(StudentSignUpRequestDto dto, Optional<String> code) {
        if (studentRepository.existsByClientId(dto.clientId())) {
            throw new UserAlreadyExistsException("이미 가입된 사용자입니다.");
        }

        Student student = studentMapper.toEntity(dto);

        if (dto.interests() != null && !dto.interests().isEmpty()) {

            List<Interest> interests = interestRepository.findAllById(dto.interests());

            student.addInterests(interests);
        }

        code.ifPresent(matchCode -> {
            Guardian guardian = guardianRepository.findByMatchCode(matchCode)
                .orElseThrow(() -> new GuardianNotFoundException("코드 '" + matchCode + "'에 해당하는 보호자를 찾을 수 없습니다."));

            guardian.addStudent(student);
        });

        Student savedstudent = studentRepository.save(student);

        String accessToken = jwtTokenProvider.createAccessToken(savedstudent.getClientId(),
            UserType.STUDENT.name());
        String refreshToken = jwtTokenProvider.createRefreshToken(savedstudent.getClientId());

        return SignUpResponseDto.builder().id(savedstudent.getId()).name(student.getName())
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