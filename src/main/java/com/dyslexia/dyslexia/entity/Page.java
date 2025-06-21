package com.dyslexia.dyslexia.entity;

import com.dyslexia.dyslexia.enums.ConvertProcessStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pages", 
        uniqueConstraints = @UniqueConstraint(columnNames = {"textbook_id", "page_number"}))
@Getter
@NoArgsConstructor
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "textbook_id", nullable = false)
    private Textbook textbook;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "original_content", columnDefinition = "TEXT")
    private String originalContent;

    @Column(name = "processed_content", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode processedContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false)
    private ConvertProcessStatus processingStatus = ConvertProcessStatus.PENDING;

    @Column(name = "section_title", columnDefinition = "TEXT")
    private String sectionTitle;

    @Column(name = "reading_level")
    private Integer readingLevel;

    @Column(name = "word_count")
    private Integer wordCount;

    @Column(name = "complexity_score")
    private Float complexityScore;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PageTip> pageTips = new ArrayList<>();

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PageImage> pageImages = new ArrayList<>();

    @Builder
    public Page(Textbook textbook, Integer pageNumber, String originalContent, JsonNode processedContent,
               String sectionTitle, Integer readingLevel, Integer wordCount, Float complexityScore,
               ConvertProcessStatus processingStatus) {
        this.textbook = textbook;
        this.pageNumber = pageNumber;
        this.originalContent = originalContent;
        this.processedContent = processedContent;
        this.sectionTitle = sectionTitle;
        this.readingLevel = readingLevel;
        this.wordCount = wordCount;
        this.complexityScore = complexityScore;
        this.processingStatus = processingStatus != null ? processingStatus : ConvertProcessStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setTextbook(Textbook textbook) {
        this.textbook = textbook;
    }

    public void setProcessingStatus(ConvertProcessStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public void addPageTip(PageTip pageTip) {
        this.pageTips.add(pageTip);
        if (pageTip.getPage() != this) {
            pageTip.setPage(this);
        }
    }

    public void addPageImage(PageImage pageImage) {
        this.pageImages.add(pageImage);
        if (pageImage.getPage() != this) {
            pageImage.setPage(this);
        }
    }
} 