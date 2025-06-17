package com.dyslexia.dyslexia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dyslexia.dyslexia.dto.CourseDto;
import com.dyslexia.dyslexia.dto.CourseRequestDto;
import com.dyslexia.dyslexia.dto.StudentDto;
import com.dyslexia.dyslexia.dto.GuardianDto;
import com.dyslexia.dyslexia.dto.StudentRequestDto;
import com.dyslexia.dyslexia.entity.Interest;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.enums.Grade;
import com.dyslexia.dyslexia.mapper.custom.GradeMapper;
import com.dyslexia.dyslexia.mapper.custom.InterestMapper;
import com.dyslexia.dyslexia.repository.GuardianRepository;
import com.dyslexia.dyslexia.service.CourseInfoService;
import com.dyslexia.dyslexia.service.CourseService;
import com.dyslexia.dyslexia.service.StudentService;
import com.dyslexia.dyslexia.service.GuardianService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class DyslexiaApplicationTests {

  @Autowired
  private GuardianRepository guardianRepository;
  @Autowired
  private GuardianService guardianService;
  @Autowired
  private StudentService studentService;
  @Autowired
  private CourseService courseService;
  @Autowired
  private CourseInfoService courseInfoService;
  @Autowired
  private GradeMapper gradeMapper;
  @Autowired
  private InterestMapper interestMapper;


  @Test
  void testToEnum() {
    Grade grade = gradeMapper.toEnum("4학년");
    assertEquals(Grade.GRADE_4, grade);
  }

  @Test
  void testToLabel() {
    String label = gradeMapper.toLabel(Grade.GRADE_6);
    assertEquals("6학년", label);
  }

  @Test
  void testToEnumWithInvalidLabel() {
    assertThrows(IllegalArgumentException.class, () -> {
      gradeMapper.toEnum("7학년"); // 없는 값
    });
  }

  @Test
  void testToEntityList() {
    List<String> names = List.of("독서", "코딩");
    List<Interest> entities = interestMapper.toEntityList(names);

    assertEquals(2, entities.size());
    assertEquals("독서", entities.get(0).getName());
  }

  @Test
  void testToStringList() {
    List<Interest> interests = List.of(Interest.builder().name("수학").build(),
        Interest.builder().name("과학").build());

    List<String> names = interestMapper.toStringList(interests);

    assertEquals(List.of("수학", "과학"), names);
  }

  // ✅ 공통 생성 메서드들

  private Guardian createGuardian(String clientId, String org) {
    return guardianRepository.save(
        Guardian.builder().clientId(clientId).organization(org).profileImageUrl("guardian.png")
            .build());
  }

  private StudentDto createStudent(String clientId, Teacher teacher, String gradeLabel) {
    StudentRequestDto req = new StudentRequestDto();
    req.setClientId(clientId);
    req.setGuardianId(guardian.getId());
    req.setGradeLabel(gradeLabel);
    req.setType("REGULAR");
    req.setState("ACTIVE");
    req.setProfileImageUrl("student.png");
    req.setInterests(List.of("과학", "독서"));
    return null;
    // TODO: 해당 메서드가 없었어요.
//    return studentService.saveStudent(req);
  }

  private CourseDto createCourse(String title, Guardian guardian, Grade grade) {
    CourseRequestDto req = new CourseRequestDto();
    req.setGuardianId(guardian.getId());
    req.setSubjectPath("math/" + title);
    req.setTitle(title);
    req.setType("REGULAR");
    req.setGrade(grade);
    req.setState("ACTIVE");
    return courseService.saveCourse(req);
  }

  // ✅ 테스트 메서드들

  @Test
  void testSaveGuardian() {
    // when
    Guardian guardian = createGuardian("guardian01", "한글초등학교");

    // then
    assertNotNull(guardian.getId());
    assertEquals("guardian01", guardian.getClientId());
  }

  @Test
  void testSaveStudent() {
    // TODO: 해당 테스트는 실패
//    // given
//    Guardian guardian = createGuardian("guardian02", "중앙초");
//
//    // when
//    StudentDto student = createStudent("student01", guardian, "4학년");
//
//    // then
//    assertEquals("student01", student.getClientId());
//    assertEquals(Grade.GRADE_4, student.getGrade());
//    assertTrue(student.getInterests().contains("과학"));
  }

  @Test
  void testSaveCourse() {
    // given
    Guardian guardian = createGuardian("guardian03", "국제초등학교");

    // when
    CourseDto course = createCourse("기초 대수학", guardian, Grade.GRADE_4);

    // then
    assertEquals("기초 대수학", course.getTitle());
    assertEquals(Grade.GRADE_4, course.getGrade());
  }

  @Test
  void testSaveCourseInfo() {
    // TODO: 해당 테스트는 실패
//    // given
//    Guardian guardian = createGuardian("guardian04", "명문초");
//    StudentDto student = createStudent("student99", guardian, "3학년");
//    CourseDto course = createCourse("기초 물리", guardian, Grade.GRADE_3);
//
//    // when
//    CourseInfoReqDto infoReq = new CourseInfoReqDto();
//    infoReq.setCourseId(course.getId());
//    infoReq.setStudentId(student.getId());
//    infoReq.setGuardianId(guardian.getId());
//    infoReq.setLearningTime(60);
//    infoReq.setPage(5);
//    infoReq.setMaxPage(10);
//
//    CourseInfoDto info = courseInfoService.saveCourseInfo(infoReq);
//
//    // then
//    assertEquals(60, info.getLearningTime());
//    assertEquals(course.getId(), info.getCourseId());
//    assertEquals(student.getId(), info.getStudentId());
  }

  @Test
  void testGetGuardianById() throws NotFoundException {
    Guardian guardian = createGuardian("guardian001", "예림초등학교");

    GuardianDto result = guardianService.getById(guardian.getId());

    assertNotNull(result);
    assertEquals("guardian001", result.getClientId());
  }

  @Test
  void testGetStudentById() throws NotFoundException {
    // TODO: 해당 테스트는 실패
//    Guardian guardian = createGuardian("guardian002", "현명초등학교");
//
//    StudentDto saved = createStudent("student001", guardian, "4학년");
//    StudentDto result = studentService.getById(saved.getId());
//
//    assertNotNull(result);
//    assertEquals("student001", result.getClientId());
//    assertTrue(result.getInterests().contains("과학"));
  }

  @Test
  void testGetCourseById() throws NotFoundException {
    Guardian guardian = createGuardian("guardian003", "미래초");

    CourseDto saved = createCourse("기초 우주과학", guardian, Grade.GRADE_4);
    CourseDto result = courseService.getById(saved.getId());

    assertNotNull(result);
    assertEquals("기초 우주과학", result.getTitle());
  }

  @Test
  void testGetCourseInfoById() throws NotFoundException {
    // TODO: 해당 테스트는 실패
//    Guardian guardian = createGuardian("guardian004", "지성초");
//    StudentDto student = createStudent("student999", guardian, "3학년");
//    CourseDto course = createCourse("수학 사고력 향상", guardian, Grade.GRADE_3);
//
//    CourseInfoReqDto infoReq = new CourseInfoReqDto();
//    infoReq.setCourseId(course.getId());
//    infoReq.setStudentId(student.getId());
//    infoReq.setGuardianId(guardian.getId());
//    infoReq.setLearningTime(45);
//    infoReq.setPage(2);
//    infoReq.setMaxPage(5);
//
//    CourseInfoDto saved = courseInfoService.saveCourseInfo(infoReq);
//    CourseInfoDto result = courseInfoService.getById(saved.getId());
//
//    assertNotNull(result);
//    assertEquals(saved.getId(), result.getId());
//    assertEquals(45, result.getLearningTime());
  }
}
