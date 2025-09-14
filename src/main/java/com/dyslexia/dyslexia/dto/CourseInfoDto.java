package com.dyslexia.dyslexia.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseInfoDto {

  private Long id;
  private String courseName;
  private String courseCode;
  private String description;
  private Integer duration;
  private String difficultyLevel;
  private Boolean isActive;
}