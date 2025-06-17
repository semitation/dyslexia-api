package com.dyslexia.dyslexia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CourseInfoRequestDto {

  private Long courseId;
  private Long studentId;
  private Long guardianId;
  private Integer learningTime;
  private Integer page;
  private Integer maxPage;
}
