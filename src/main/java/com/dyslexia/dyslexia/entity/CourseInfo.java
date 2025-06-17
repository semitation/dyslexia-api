package com.dyslexia.dyslexia.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "courseInfo")
@Getter
@NoArgsConstructor
public class CourseInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "courseId")
  private Course course;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "studentId")
  private Student student;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "guardianId")
  private Guardian guardian;

  private Integer learningTime;

  private Integer page;

  private Integer maxPage;

  @Builder
  public CourseInfo(Course course, Student student, Guardian guardian, Integer learningTime,
      Integer page, Integer maxPage) {
    this.course = course;
    this.student = student;
    this.guardian = guardian;
    this.learningTime = learningTime;
    this.page = page;
    this.maxPage = maxPage;
  }
}