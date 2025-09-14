package com.dyslexia.dyslexia.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "course_info")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "course_name", nullable = false)
  private String courseName;

  @Column(name = "course_code")
  private String courseCode;

  @Column(name = "description")
  private String description;

  @Column(name = "duration")
  private Integer duration;

  @Column(name = "difficulty_level")
  private String difficultyLevel;

  @Column(name = "is_active")
  private Boolean isActive = true;

  @Builder
  public CourseInfo(String courseName, String courseCode, String description,
                    Integer duration, String difficultyLevel, Boolean isActive) {
    this.courseName = courseName;
    this.courseCode = courseCode;
    this.description = description;
    this.duration = duration;
    this.difficultyLevel = difficultyLevel;
    this.isActive = isActive != null ? isActive : true;
  }
}