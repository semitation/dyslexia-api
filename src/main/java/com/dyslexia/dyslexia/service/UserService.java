package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.SignUpRequestDto;
import com.dyslexia.dyslexia.dto.SignUpResponseDto;
import com.dyslexia.dyslexia.entity.Interest;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.entity.Teacher;
import java.util.List;
import com.dyslexia.dyslexia.enums.UserType;
import com.dyslexia.dyslexia.exception.UserAlreadyExistsException;
import com.dyslexia.dyslexia.mapper.StudentMapper;
import com.dyslexia.dyslexia.mapper.TeacherMapper;
import com.dyslexia.dyslexia.repository.InterestRepository;
import com.dyslexia.dyslexia.repository.StudentRepository;
import com.dyslexia.dyslexia.repository.TeacherRepository;
import com.dyslexia.dyslexia.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final InterestRepository interestRepository;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto dto) {
        log.info("회원가입 요청 처리 중: 사용자 유형={}", dto.getUserType());

        if (UserType.TEACHER == dto.getUserType()) {
            return registerTeacher(dto);
        } else {
            return registerStudent(dto);
        }
    }

    private SignUpResponseDto registerTeacher(SignUpRequestDto dto) {
        if (teacherRepository.existsByClientId(dto.getClientId())) {
            throw new UserAlreadyExistsException("이미 가입된 사용자입니다.");
        }

        Teacher teacher = teacherRepository.save(teacherMapper.toEntity(dto));

        teacher.generateMatchCode();

        while (teacherRepository.existsByMatchCodeAndIdNot(teacher.getMatchCode(),
            teacher.getId())) {
            teacher.generateMatchCode();
        }

        String accessToken = jwtTokenProvider.createAccessToken(teacher.getClientId(),
            UserType.TEACHER.name());
        String refreshToken = jwtTokenProvider.createRefreshToken(teacher.getClientId());

        return SignUpResponseDto.builder().id(teacher.getId()).name(teacher.getName())
            .userType(UserType.TEACHER).accessToken(accessToken).refreshToken(refreshToken).build();
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

        log.info("학생 등록 성공: ID={}", student.getId());

        return SignUpResponseDto.builder().id(student.getId()).name(student.getName())
            .userType(UserType.STUDENT).accessToken(accessToken).refreshToken(refreshToken).build();
    }
}