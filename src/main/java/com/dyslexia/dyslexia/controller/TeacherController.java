package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.StudentDto;
import com.dyslexia.dyslexia.dto.TeacherCodeDto;
import com.dyslexia.dyslexia.dto.TeacherDto;
import com.dyslexia.dyslexia.service.StudentService;
import com.dyslexia.dyslexia.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

  private final TeacherService teacherService;
  private final StudentService studentService;

  @Operation(summary = "id로 교사 조회")
  @GetMapping("/{id}")
  public ResponseEntity<TeacherDto> getById(@PathVariable long id) {
    return ResponseEntity.ok(teacherService.getById(id));
  }

  @Operation(summary = "client id로 교사 조회")
  @GetMapping
  public ResponseEntity<TeacherDto> getByClientId(@RequestParam String clientId) {
    return ResponseEntity.ok(teacherService.getTeacherByClientId(clientId));
  }

  @Operation(summary = "id로 매칭 코드 조회")
  @GetMapping("/code/{id}")
  public ResponseEntity<TeacherCodeDto> getCodeById(@PathVariable long id)
      throws NotFoundException {
    return ResponseEntity.ok(teacherService.getCodeById(id));
  }

  @Operation(summary = "id로 담당 학생 조회")
  @GetMapping("/{teacherId}/students")
  public ResponseEntity<List<StudentDto>> getStudentsByTeacherId(@PathVariable Long teacherId) {
    return ResponseEntity.ok(studentService.getStudentsByTeacher(teacherId));
  }
}
