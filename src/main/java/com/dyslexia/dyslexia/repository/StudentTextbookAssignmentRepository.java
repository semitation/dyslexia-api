package com.dyslexia.dyslexia.repository;

import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.entity.Student;
import com.dyslexia.dyslexia.entity.StudentTextbookAssignment;
import com.dyslexia.dyslexia.entity.Textbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentTextbookAssignmentRepository extends JpaRepository<StudentTextbookAssignment, Long> {
    
    List<StudentTextbookAssignment> findByStudent(Student student);
    
    List<StudentTextbookAssignment> findByStudentId(Long studentId);
    
    List<StudentTextbookAssignment> findByTextbook(Textbook textbook);
    
    List<StudentTextbookAssignment> findByTextbookId(Long textbookId);
    
    Optional<StudentTextbookAssignment> findByStudentIdAndTextbookId(Long studentId, Long textbookId);
    
    List<StudentTextbookAssignment> findByAssignedBy(Guardian guardian);
    
    List<StudentTextbookAssignment> findByAssignedById(Long guardianId);

    List<StudentTextbookAssignment> findByAssignedByIdAndStudentId(Long guardianId, Long studentId);
    
    Long countByTextbookId(Long textbookId);
} 