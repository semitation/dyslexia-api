package com.dyslexia.dyslexia.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "teachers")
@Getter
@NoArgsConstructor
public class Teacher {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String clientId;

  private String organization;

  private String profileImageUrl;

  @Builder
  public Teacher(String clientId, String organization, String profileImageUrl) {
    this.clientId = clientId;
    this.organization = organization;
    this.profileImageUrl = profileImageUrl;
  }
}
