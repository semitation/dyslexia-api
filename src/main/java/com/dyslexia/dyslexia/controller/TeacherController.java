package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.TeacherCodeDto;
import com.dyslexia.dyslexia.dto.TeacherDto;
import com.dyslexia.dyslexia.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
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

  @GetMapping("/{id}")
  public ResponseEntity<TeacherDto> getById(@PathVariable long id) {
    return ResponseEntity.ok(teacherService.getById(id));
  }

  @GetMapping
  public ResponseEntity<TeacherDto> getByClientId(@RequestParam String clientId) {
    return ResponseEntity.ok(teacherService.getTeacherByClientId(clientId));
  }

  @GetMapping("/code/{id}")
  public ResponseEntity<TeacherCodeDto> getCodeById(@PathVariable long id)
      throws NotFoundException {
    return ResponseEntity.ok(teacherService.getCodeById(id));
  }
}
