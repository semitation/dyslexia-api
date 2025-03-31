package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.dto.CourseInfoDto;
import com.dyslexia.dyslexia.service.CourseInfoService;
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
@RequestMapping("/courseInfo")
@RequiredArgsConstructor
public class CourseInfoController {

  private final CourseInfoService courseInfoService;

  @PostMapping
  public ResponseEntity<CourseInfoDto> createCourseInfo(@RequestBody CourseInfoDto dto) {
    return ResponseEntity.ok(courseInfoService.saveCourseInfo(dto));
  }

  @GetMapping("/by-student/{studentId}")
  public ResponseEntity<List<CourseInfoDto>> getByStudent(@PathVariable Long studentId) {
    return ResponseEntity.ok(courseInfoService.getInfosByStudent(studentId));
  }
}

