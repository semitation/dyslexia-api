package com.dyslexia.dyslexia.repository;

import com.dyslexia.dyslexia.entity.Textbook;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TextbookRepository extends JpaRepository<Textbook, Long> {

  List<Textbook> findAllByOrderByUpdatedAtDesc();

  List<Textbook> findAllByOrderByCreatedAtDesc();

  List<Textbook> findByGuardianIdOrderByUpdatedAtDesc(Long guardianId);

  List<Textbook> findByGuardianIdOrderByCreatedAtDesc(Long guardianId);

  @Query("SELECT t FROM Textbook t WHERE t.document.id = :documentId")
  java.util.Optional<Textbook> findByDocumentId(@Param("documentId") Long documentId);

  @Query("SELECT t FROM Textbook t WHERE t.document.id = :documentId AND t.guardian.id = :guardianId")
  java.util.Optional<Textbook> findByDocumentIdAndGuardianId(@Param("documentId") Long documentId, @Param("guardianId") Long guardianId);
}
