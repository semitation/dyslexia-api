package com.dyslexia.dyslexia.repository;

import com.dyslexia.dyslexia.entity.Document;
import com.dyslexia.dyslexia.entity.Teacher;
import com.dyslexia.dyslexia.enums.ConvertProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByTeacher(Teacher teacher);
    
    List<Document> findByTeacherId(Long teacherId);
    
    Optional<Document> findByIdAndTeacherId(Long id, Long teacherId);
    
    List<Document> findByTeacherIdOrderByUploadedAtDesc(Long teacherId);
    
    List<Document> findAllByOrderByUploadedAtDesc();
} 