package com.dyslexia.dyslexia.repository;

import com.dyslexia.dyslexia.entity.Textbook;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TextbookRepository extends JpaRepository<Textbook, Long> {

  List<Textbook> findAllByOrderByUpdatedAtDesc();

  List<Textbook> findAllByOrderByCreatedAtDesc();

  List<Textbook> findByGuardianIdOrderByUpdatedAtDesc(Long guardianId);

  List<Textbook> findByGuardianIdOrderByCreatedAtDesc(Long guardianId);

  java.util.Optional<Textbook> findByDocumentId(Long documentId);

  java.util.Optional<Textbook> findByDocumentIdAndGuardianId(Long documentId, Long guardianId);
}
