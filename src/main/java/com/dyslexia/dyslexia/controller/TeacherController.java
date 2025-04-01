package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.TeacherDto;
import com.dyslexia.dyslexia.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

  private final TeacherService teacherService;

  @PostMapping
  public ResponseEntity<TeacherDto> createTeacher(@RequestBody TeacherDto dto) {
    return ResponseEntity.ok(teacherService.saveTeacher(dto));
  }

  @GetMapping
  public ResponseEntity<TeacherDto> getById(@RequestParam long id) {
    return teacherService.getTeacherById(id).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<TeacherDto> getByClientId(@RequestParam String clientId) {
    return teacherService.getTeacherByClientId(clientId).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
