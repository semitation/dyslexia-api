package com.dyslexia.dyslexia.dto;

import lombok.Data;

@Data
public class CourseReqDto {

  private String courseName;
  private String courseCode;
  private String description;
  private Integer creditHours;
  private String instructor;
  private Boolean isActive;
}