package com.dyslexia.dyslexia.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherDto {

  private Long id;
  private String teacherCode;
  private String name;
  private String email;
  private String phone;
  private String schoolName;
  private String department;
  private Boolean isActive;
}