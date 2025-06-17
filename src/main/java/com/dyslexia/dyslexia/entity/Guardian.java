package com.dyslexia.dyslexia.entity;

import com.dyslexia.dyslexia.enums.GuardianRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "guardians")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Guardian {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String clientId;

  private String name;

  private String organization;

  private String email;

  private String profileImageUrl;

  private GuardianRole guardianRole;

  @Column(unique = true)
  private String matchCode;

  @OneToMany(mappedBy = "guardian", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<Student> students = new ArrayList<>();

  @Builder
  public Guardian(String clientId, String name, String organization, String profileImageUrl) {
    this.clientId = clientId;
    this.name = name;
    this.organization = organization;
    this.profileImageUrl = profileImageUrl;
  }

  public void generateMatchCode() {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder code = new StringBuilder();
    Random random = new Random();

    for (int i = 0; i < 6; i++) {
      code.append(chars.charAt(random.nextInt(chars.length())));
    }

    this.matchCode = code.toString();
  }

  public void addStudent(Student student) {
    this.students.add(student);

    if (student.getGuardian() != this) {
      student.setGuardian(this);
    }
  }
}
