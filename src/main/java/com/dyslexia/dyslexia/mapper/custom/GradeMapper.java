package com.dyslexia.dyslexia.mapper.custom;

import com.dyslexia.dyslexia.enums.Grade;
import org.springframework.stereotype.Component;

@Component
public class GradeMapper {
  public Grade toEnum(String name) {
    return name == null ? null : Grade.valueOf(name);
  }

  public String toLabel(Grade grade) {
    return grade == null ? null : grade.name();
  }
}