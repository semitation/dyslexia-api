package com.dyslexia.dyslexia.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseDto {

  private Long id;
  private String courseName;
  private String courseCode;
  private String description;
  private Integer creditHours;
  private String instructor;
  private Boolean isActive;
}