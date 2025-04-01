package com.dyslexia.dyslexia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dyslexia.dyslexia.dto.CourseDto;
import com.dyslexia.dyslexia.dto.CourseInfoDto;
import com.dyslexia.dyslexia.dto.CourseInfoReqDto;
import com.dyslexia.dyslexia.dto.CourseReqDto;
import com.dyslexia.dyslexia.dto.StudentDto;
import com.dyslexia.dyslexia.dto.StudentReqDto;
import com.dyslexia.dyslexia.entity.Course;
import com.dyslexia.dyslexia.entity.Teacher;
import com.dyslexia.dyslexia.enums.Grade;
import com.dyslexia.dyslexia.repository.CourseRepository;
import com.dyslexia.dyslexia.repository.InterestRepository;
import com.dyslexia.dyslexia.repository.TeacherRepository;
import com.dyslexia.dyslexia.service.CourseInfoService;
import com.dyslexia.dyslexia.service.CourseService;
import com.dyslexia.dyslexia.service.StudentService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class DyslexiaApplicationTests {

  @Autowired private TeacherRepository teacherRepository;
  @Autowired private InterestRepository interestRepository;
  @Autowired private StudentService studentService;
  @Autowired private CourseService courseService;
  @Autowired private CourseInfoService courseInfoService;

  @Test
  void testSaveTeacher() {
    Teacher teacher = teacherRepository.save(
        Teacher.builder()
            .clientId("teacher01")
            .organization("한글초등학교")
            .profileImageUrl("teacher.png")
            .build()
    );

    assertNotNull(teacher.getId());
    assertEquals("teacher01", teacher.getClientId());
  }

  @Test
  void testSaveStudent() {
    Teacher teacher = teacherRepository.save(
        Teacher.builder()
            .clientId("teacher02")
            .organization("중앙초")
            .profileImageUrl("teacher.png")
            .build()
    );

    StudentReqDto req = new StudentReqDto();
    req.setClientId("student01");
    req.setTeacherId(teacher.getId());
    req.setGradeLabel("4학년");
    req.setType("REGULAR");
    req.setState("ACTIVE");
    req.setProfileImageUrl("student.png");
    req.setInterests(List.of("과학", "독서"));

    StudentDto student = studentService.saveStudent(req);

    assertEquals("student01", student.getClientId());
    assertEquals(Grade.ELEMENTARY_4, student.getGrade());
    assertTrue(student.getInterests().contains("과학"));
  }

  @Test
  void testSaveCourse() {
    Teacher teacher = teacherRepository.save(
        Teacher.builder()
            .clientId("teacher03")
            .organization("국제초등학교")
            .profileImageUrl("teacher.png")
            .build()
    );

    CourseReqDto req = new CourseReqDto();
    req.setTeacherId(teacher.getId());
    req.setSubjectPath("math/algebra");
    req.setTitle("기초 대수학");
    req.setType("REGULAR");
    req.setGrade(Grade.ELEMENTARY_4);
    req.setState("ACTIVE");

    CourseDto course = courseService.saveCourse(req);

    assertEquals("기초 대수학", course.getTitle());
    assertEquals(Grade.ELEMENTARY_4, course.getGrade());
  }

  @Test
  void testSaveCourseInfo() {
    // 준비: 교사, 학생, 과목 먼저 생성
    Teacher teacher = teacherRepository.save(
        Teacher.builder().clientId("teacher04").organization("명문초").profileImageUrl("img.png").build()
    );

    StudentReqDto studentReq = new StudentReqDto();
    studentReq.setClientId("student99");
    studentReq.setTeacherId(teacher.getId());
    studentReq.setGradeLabel("3학년");
    studentReq.setType("REGULAR");
    studentReq.setState("ACTIVE");
    studentReq.setProfileImageUrl("student.png");
    studentReq.setInterests(List.of("수학", "코딩"));
    StudentDto student = studentService.saveStudent(studentReq);

    CourseReqDto courseReq = new CourseReqDto();
    courseReq.setTeacherId(teacher.getId());
    courseReq.setSubjectPath("science/physics");
    courseReq.setTitle("기초 물리");
    courseReq.setType("REGULAR");
    courseReq.setGrade(Grade.ELEMENTARY_3);
    courseReq.setState("ACTIVE");
    CourseDto course = courseService.saveCourse(courseReq);

    // 수강 정보 저장
    CourseInfoReqDto infoReq = new CourseInfoReqDto();
    infoReq.setCourseId(course.getId());
    infoReq.setStudentId(student.getId());
    infoReq.setTeacherId(teacher.getId());
    infoReq.setLearningTime(60);
    infoReq.setPage(5);
    infoReq.setMaxPage(10);

    CourseInfoDto info = courseInfoService.saveCourseInfo(infoReq);

    assertEquals(60, info.getLearningTime());
    assertEquals(course.getId(), info.getCourseId());
    assertEquals(student.getId(), info.getStudentId());
  }
}
