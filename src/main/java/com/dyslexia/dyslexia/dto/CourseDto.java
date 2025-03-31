package com.dyslexia.dyslexia.dto;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseDto {
  private Long id;
  private Long teacherId;
  private String subjectPath;
  private String title;
  private String type;
  private Integer grade;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String state;
}
