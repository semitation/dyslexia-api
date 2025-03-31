package com.dyslexia.dyslexia.repository;

import com.dyslexia.dyslexia.entity.Student;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
  List<Student> findByTeacherId(Long teacherId);
}