package com.dyslexia.dyslexia.entity;

import com.dyslexia.dyslexia.enums.Grade;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "students")
@Getter
@NoArgsConstructor
public class Student {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String clientId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "teacherId")
  private Teacher teacher;

  @Enumerated(EnumType.STRING)
  private Grade grade;

  private String type;

  private String interested;

  private String state;

  private String profileImageUrl;

  @Builder
  public Student(String clientId, Teacher teacher, Grade grade, String type, String interested,
      String state, String profileImageUrl) {
    this.clientId = clientId;
    this.teacher = teacher;
    this.grade = grade;
    this.type = type;
    this.interested = interested;
    this.state = state;
    this.profileImageUrl = profileImageUrl;
  }
}
