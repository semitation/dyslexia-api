package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.MatchResponseDto;
import com.dyslexia.dyslexia.dto.StudentDto;
import com.dyslexia.dyslexia.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

  private final StudentService studentService;

  @GetMapping("/{id}")
  public ResponseEntity<StudentDto> getById(@PathVariable Long id) {
    return ResponseEntity.ok(studentService.getById(id));
  }

  @GetMapping
  public ResponseEntity<StudentDto> getByClientId(@RequestParam String clientId) {
    return ResponseEntity.ok(studentService.getByClientId(clientId));
  }

  @PostMapping("/match/{id}")
  public ResponseEntity<MatchResponseDto> matchByCode(@PathVariable Long id, @RequestParam String code) {
    return ResponseEntity.ok(studentService.matchByCode(id, code));
  }
}
