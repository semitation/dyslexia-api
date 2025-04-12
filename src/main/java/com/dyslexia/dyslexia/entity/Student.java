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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "students")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String clientId;

  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "teacherId")
  private Teacher teacher;

  @Enumerated(EnumType.STRING)
  private Grade grade;

  private String type;
  private String state;
  private String profileImageUrl;

  @ManyToMany
  @JoinTable(name = "student_interest", joinColumns = @JoinColumn(name = "studentId"), inverseJoinColumns = @JoinColumn(name = "interestId"))
  private List<Interest> interests = new ArrayList<>();

  @Builder
  public Student(String clientId, String name, Teacher teacher, Grade grade, String type, String state,
      String profileImageUrl, List<Interest> interests) {
    this.clientId = clientId;
    this.name = name;
    this.teacher = teacher;
    this.grade = grade;
    this.type = type;
    this.state = state;
    this.profileImageUrl = profileImageUrl;
    this.interests = interests;
  }

  public void setTeacher(Teacher teacher) {
    this.teacher = teacher;

    if (teacher != null && !teacher.getStudents().contains(this)) {
      teacher.getStudents().add(this);
    }
  }

  public void addInterests(List<Interest> interests) {

    if (this.interests == null) {
      this.interests = new ArrayList<>();
    }

    for (Interest interest : interests) {
      if (!this.interests.contains(interest)) {
        this.interests.add(interest);
      }
    }
  }
}
