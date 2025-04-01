package com.dyslexia.dyslexia.enums;

import lombok.Getter;

@Getter
public enum Grade {
  ELEMENTARY_1("초1"),
  ELEMENTARY_2("초2"),
  ELEMENTARY_3("초3"),
  ELEMENTARY_4("초4"),
  ELEMENTARY_5("초5"),
  ELEMENTARY_6("초6");

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
