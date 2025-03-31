package com.dyslexia.dyslexia.dto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentDto {
  private Long id;
  private String clientId;
  private Long teacherId;
  private Integer grade;
  private String type;
  private String interested;
  private boolean state;
  private String profileImageUrl;
}
