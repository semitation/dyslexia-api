package com.dyslexia.dyslexia.repository;

import com.dyslexia.dyslexia.entity.Document;
import com.dyslexia.dyslexia.entity.Guardian;
import com.dyslexia.dyslexia.enums.DocumentProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByGuardian(Guardian guardian);
    
    List<Document> findByGuardianId(Long guardianId);
    
    List<Document> findByProcessStatus(DocumentProcessStatus status);
    
    Optional<Document> findByIdAndGuardianId(Long id, Long guardianId);
    
    List<Document> findByGuardianIdOrderByCreatedAtDesc(Long guardianId);
    
    List<Document> findAllByOrderByCreatedAtDesc();
} 