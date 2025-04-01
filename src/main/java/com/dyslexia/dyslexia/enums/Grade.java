package com.dyslexia.dyslexia.enums;

import lombok.Getter;

@Getter
public enum Grade {
  ELEMENTARY_1("1학년"),
  ELEMENTARY_2("2학년"),
  ELEMENTARY_3("3학년"),
  ELEMENTARY_4("4학년"),
  ELEMENTARY_5("5학년"),
  ELEMENTARY_6("6학년");

  private final String label;

  Grade(String label) {
    this.label = label;
  }

  public static Grade fromLabel(String label) {
    for (Grade grade : Grade.values()) {
      if (grade.label.equals(label)) {
        return grade;
      }
    }

    throw new IllegalArgumentException("Unknown grade label: " + label);
  }
}
