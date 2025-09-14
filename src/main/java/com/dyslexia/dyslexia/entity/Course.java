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
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "course_name", nullable = false)
  private String courseName;

  @Column(name = "course_code")
  private String courseCode;

  @Column(name = "description")
  private String description;

  @Column(name = "credit_hours")
  private Integer creditHours;

  @Column(name = "instructor")
  private String instructor;

  @Column(name = "is_active")
  private Boolean isActive = true;

  @Builder
  public Course(String courseName, String courseCode, String description,
                Integer creditHours, String instructor, Boolean isActive) {
    this.courseName = courseName;
    this.courseCode = courseCode;
    this.description = description;
    this.creditHours = creditHours;
    this.instructor = instructor;
    this.isActive = isActive != null ? isActive : true;
  }
}