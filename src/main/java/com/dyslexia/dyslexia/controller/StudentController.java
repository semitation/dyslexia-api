package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.StudentDto;
import com.dyslexia.dyslexia.service.StudentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

  private final StudentService studentService;

  @PostMapping
  public ResponseEntity<StudentDto> createStudent(@RequestBody StudentDto dto) {
    return ResponseEntity.ok(studentService.saveStudent(dto));
  }

  @GetMapping("/teacher/{teacherId}")
  public ResponseEntity<List<StudentDto>> getStudentsByTeacher(@PathVariable Long teacherId) {
    return ResponseEntity.ok(studentService.getStudentsByTeacher(teacherId));
  }
}
