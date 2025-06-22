package com.dyslexia.dyslexia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dyslexia.dyslexia.entity.AIRequest;

@Repository
public interface AIRequestRepository extends JpaRepository<AIRequest, Long> {
}
