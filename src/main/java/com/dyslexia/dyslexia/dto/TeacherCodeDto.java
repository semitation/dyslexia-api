package com.dyslexia.dyslexia.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherCodeDto {

  private String teacherCode;
  private String name;
  private String schoolName;
}