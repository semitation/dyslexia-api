package com.dyslexia.dyslexia.repository;

import com.dyslexia.dyslexia.entity.Guardian;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardianRepository extends JpaRepository<Guardian, Long> {
  Optional<Guardian> findByClientId(String clientId);

    boolean existsByMatchCodeAndIdNot(String matchCode, Long id);
    boolean existsByClientId(String clientId);

  Optional<Guardian> findByMatchCode(String code);
}