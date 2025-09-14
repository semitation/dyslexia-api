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
@Table(name = "teachers")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Teacher {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "teacher_code", unique = true, nullable = false)
  private String teacherCode;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "email")
  private String email;

  @Column(name = "phone")
  private String phone;

  @Column(name = "school_name")
  private String schoolName;

  @Column(name = "department")
  private String department;

  @Column(name = "is_active")
  private Boolean isActive = true;

  @Builder
  public Teacher(String teacherCode, String name, String email,
                 String phone, String schoolName, String department, Boolean isActive) {
    this.teacherCode = teacherCode;
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.schoolName = schoolName;
    this.department = department;
    this.isActive = isActive != null ? isActive : true;
  }
}