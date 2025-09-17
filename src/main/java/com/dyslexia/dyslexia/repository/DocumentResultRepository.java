package com.dyslexia.dyslexia.repository;

import com.dyslexia.dyslexia.entity.DocumentResult;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentResultRepository extends JpaRepository<DocumentResult, Long> {
    Optional<DocumentResult> findByJobId(String jobId);
    boolean existsByJobId(String jobId);
}

