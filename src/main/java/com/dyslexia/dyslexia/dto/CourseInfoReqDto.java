package com.dyslexia.dyslexia.dto;

import lombok.Data;

@Data
public class CourseInfoReqDto {

  private String courseName;
  private String courseCode;
  private String description;
  private Integer duration;
  private String difficultyLevel;
  private Boolean isActive;
}