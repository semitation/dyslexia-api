package com.dyslexia.dyslexia.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dyslexia.dyslexia.dto.GuardianSignUpRequestDto;
import com.dyslexia.dyslexia.dto.SignUpResponseDto;
import com.dyslexia.dyslexia.dto.StudentSignUpRequestDto;
import com.dyslexia.dyslexia.entity.Interest;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.enums.Grade;
import com.dyslexia.dyslexia.enums.GuardianRole;
import com.dyslexia.dyslexia.enums.UserType;
import com.dyslexia.dyslexia.exception.ApplicationException;
import com.dyslexia.dyslexia.exception.ExceptionCode;
import com.dyslexia.dyslexia.mapper.StudentMapper;
import com.dyslexia.dyslexia.mapper.GuardianMapper;
import com.dyslexia.dyslexia.repository.InterestRepository;
import com.dyslexia.dyslexia.repository.StudentRepository;
import com.dyslexia.dyslexia.repository.GuardianRepository;
import com.dyslexia.dyslexia.util.JwtTokenProvider;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private GuardianRepository guardianRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private GuardianMapper guardianMapper;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    private GuardianSignUpRequestDto guardianDto;
    private StudentSignUpRequestDto studentDto;

    @BeforeEach
    void setUp() {
        guardianDto = GuardianSignUpRequestDto.builder()
            .clientId("guardian123")
            .name("테스트보호자")
            .email("test@test")
            .guardianRole(GuardianRole.PARENT)
            .organization("test학교")
            .build();

        studentDto = StudentSignUpRequestDto.builder()
            .clientId("student123")
            .name("테스트학생")
            .grade(Grade.GRADE_1)
            .interests(List.of(1L, 2L))
            .build();
    }

    @Test
    void 보호자_회원가입_성공() {

        //Given
        Guardian guardian = Guardian.builder()
            .clientId("guardian123")
            .name("테스트보호자")
            .build();
        ReflectionTestUtils.setField(guardian, "id", 1L);

        when(guardianRepository.existsByClientId(anyString())).thenReturn(false);
        when(guardianMapper.toEntity((GuardianSignUpRequestDto) any())).thenReturn(guardian);
        when(guardianRepository.save(any())).thenReturn(guardian);
        when(jwtTokenProvider.createAccessToken(anyString(), anyString())).thenReturn("테스트 액세스토큰");
        when(jwtTokenProvider.createRefreshToken(anyString())).thenReturn("테스트 리프레쉬토큰");

        //When
        SignUpResponseDto response = userService.registerGuardian(guardianDto);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("테스트보호자");
        assertThat(response.getUserType()).isEqualTo(UserType.GUARDIAN);
        verify(guardianRepository).save(any());
    }

    @Test
    void 학생_회원가입_성공() {

        //Given
        Student student = Student.builder()
            .clientId("student123")
            .name("테스트학생")
            .build();
        ReflectionTestUtils.setField(student, "id", 1L);

        List<Interest> interests = Arrays.asList(
            Interest.builder().name("관심사1").build(),
            Interest.builder().name("관심사2").build()
        );
        ReflectionTestUtils.setField(interests.get(0), "id", 1L);
        ReflectionTestUtils.setField(interests.get(1), "id", 1L);

        when(studentRepository.existsByClientId(anyString())).thenReturn(false);
        when(studentMapper.toEntity(any())).thenReturn(student);
        when(interestRepository.findAllById(anyList())).thenReturn(interests);
        when(studentRepository.save(any())).thenReturn(student);
        when(jwtTokenProvider.createAccessToken(anyString(), anyString())).thenReturn("테스트 액세스토큰");
        when(jwtTokenProvider.createRefreshToken(anyString())).thenReturn("테스트 리프레쉬토큰");

        //When
        SignUpResponseDto response = userService.registerStudent(studentDto, null);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("테스트학생");
        assertThat(response.getUserType()).isEqualTo(UserType.STUDENT);
        verify(studentRepository).save(any());
        verify(interestRepository).findAllById(anyList());
    }

    @Test
    void 학생_회원가입_매칭_성공() {

        //Given
        Student student = Student.builder()
            .clientId("student123")
            .name("테스트학생")
            .build();
        ReflectionTestUtils.setField(student, "id", 1L);

        String matchCode = "ABC123";

        Guardian guardian = new Guardian("guardian1", "테스트보호자", "테스트학교", "http://example.com/profile.jpg");
        ReflectionTestUtils.setField(guardian, "id", 1L);
        ReflectionTestUtils.setField(guardian, "matchCode", matchCode);

        List<Interest> interests = Arrays.asList(
            Interest.builder().name("관심사1").build(),
            Interest.builder().name("관심사2").build()
        );
        ReflectionTestUtils.setField(interests.get(0), "id", 1L);
        ReflectionTestUtils.setField(interests.get(1), "id", 2L);

        when(guardianRepository.findByMatchCode(matchCode)).thenReturn(Optional.of(guardian));
        when(studentRepository.existsByClientId(anyString())).thenReturn(false);
        when(studentMapper.toEntity(any())).thenReturn(student);
        when(interestRepository.findAllById(anyList())).thenReturn(interests);
        when(studentRepository.save(any())).thenReturn(student);
        when(jwtTokenProvider.createAccessToken(anyString(), anyString())).thenReturn("테스트 액세스토큰");
        when(jwtTokenProvider.createRefreshToken(anyString())).thenReturn("테스트 리프레쉬토큰");

        //When
        SignUpResponseDto response = userService.registerStudent(studentDto, Optional.of(matchCode));

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("테스트학생");
        assertThat(response.getUserType()).isEqualTo(UserType.STUDENT);
        verify(studentRepository).save(any());
        verify(interestRepository).findAllById(anyList());
        verify(guardianRepository).findByMatchCode(anyString());
    }

    @Test
    void 보호자_회원가입_실패() {

        //Given
        when(guardianRepository.existsByClientId(anyString())).thenReturn(true);

        //When & Then
        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> userService.registerGuardian(guardianDto));

        Assertions.assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.USER_ALREADY_EXISTS);
        Assertions.assertThat(exception.getMessage()).isEqualTo("이미 가입된 사용자입니다.");
    }
}
