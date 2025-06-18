package com.dyslexia.dyslexia.repository;

import com.dyslexia.dyslexia.entity.Textbook;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TextbookRepository extends JpaRepository<Textbook, Long> {

}
