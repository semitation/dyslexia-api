package com.dyslexia.dyslexia.repository;

import com.dyslexia.dyslexia.entity.Document;
import com.dyslexia.dyslexia.entity.Page;
import com.dyslexia.dyslexia.entity.Textbook;
import com.dyslexia.dyslexia.enums.ConvertProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    
    List<Page> findByTextbook(Textbook textbook);
    
    List<Page> findByTextbookId(Long textbookId);
    
    Optional<Page> findByTextbookAndPageNumber(Textbook textbook, Integer pageNumber);
    
    List<Page> findByProcessingStatus(ConvertProcessStatus status);
    
    List<Page> findByTextbookIdOrderByPageNumberAsc(Long textbookId);
} 