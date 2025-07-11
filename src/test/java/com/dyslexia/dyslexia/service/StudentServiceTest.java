package com.dyslexia.dyslexia.service;

import com.dyslexia.dyslexia.dto.MatchResponseDto;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.exception.ApplicationException;
import com.dyslexia.dyslexia.exception.ExceptionCode;
import com.dyslexia.dyslexia.mapper.GuardianMapper;
import com.dyslexia.dyslexia.repository.StudentRepository;
import com.dyslexia.dyslexia.repository.GuardianRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private GuardianRepository guardianRepository;

    @Mock
    private GuardianMapper guardianMapper;

    @InjectMocks
    private StudentService studentService;

    @Test
    @DisplayName("매칭코드로 학생과 보호자 연결 성공")
    void 매칭코드로_학생과_보호자_연결_성공() {
        // Given
        Long studentId = 1L;
        String matchCode = "ABC123";

        Guardian guardian = new Guardian("guardian1", "테스트보호자", "테스트학교", "http://example.com/profile.jpg");
        ReflectionTestUtils.setField(guardian, "id", 1L);
        ReflectionTestUtils.setField(guardian, "matchCode", matchCode);

        Student student = Student.builder().clientId("student1").build();
        ReflectionTestUtils.setField(student, "id", 1L);

        MatchResponseDto expectedResponse = MatchResponseDto.builder().id(1L)
            .organization("테스트학교")
            .profileImageUrl("http://example.com/profile.jpg")
            .matchCode(matchCode).build();

        when(guardianRepository.findByMatchCode(matchCode)).thenReturn(Optional.of(guardian));
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(guardianMapper.toMatchResponseDto(guardian)).thenReturn(expectedResponse);

        // When
        MatchResponseDto result = studentService.matchByCode(studentId, matchCode);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOrganization()).isEqualTo("테스트학교");

        assertThat(guardian.getStudents()).contains(student);
        assertThat(student.getGuardian()).isEqualTo(guardian);

        verify(guardianRepository).findByMatchCode(matchCode);
        verify(studentRepository).findById(studentId);
        verify(guardianMapper).toMatchResponseDto(guardian);
    }

    @Test
    @DisplayName("존재하지 않는 매칭코드로 요청 시 예외 발생")
    void 존재하지_않는_매칭코드로_요청_시_예외_발생() {
        // Given
        Long studentId = 1L;
        String invalidCode = "INVALID";

        when(guardianRepository.findByMatchCode(invalidCode)).thenReturn(Optional.empty());

        // When & Then
        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> studentService.matchByCode(studentId, invalidCode));

        assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.GUARDIAN_NOT_FOUND);
        verify(guardianRepository).findByMatchCode(invalidCode);
        verify(studentRepository, never()).findById(any());
    }

    @Test
    @DisplayName("존재하지 않는 학생ID로 요청 시 예외 발생")
    void 존재하지_않는_학생ID로_요청_시_예외_발생() {
        // Given
        Long invalidStudentId = 999L;
        String matchCode = "ABC123";

        Guardian guardian = new Guardian("guardian1", "테스트보호자", "테스트학교", "http://example.com/profile.jpg");
        ReflectionTestUtils.setField(guardian, "matchCode", matchCode);

        when(guardianRepository.findByMatchCode(matchCode)).thenReturn(Optional.of(guardian));
        when(studentRepository.findById(invalidStudentId)).thenReturn(Optional.empty());

        // When & Then
        ApplicationException exception = assertThrows(ApplicationException.class,
            () -> studentService.matchByCode(invalidStudentId, matchCode));

        assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.STUDENT_NOT_FOUND);
        verify(guardianRepository).findByMatchCode(matchCode);
        verify(studentRepository).findById(invalidStudentId);
    }
}
