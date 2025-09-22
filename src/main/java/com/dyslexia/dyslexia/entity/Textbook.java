package com.dyslexia.dyslexia.entity;

import com.dyslexia.dyslexia.enums.ConvertProcessStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "textbooks")
@Getter
@NoArgsConstructor
public class Textbook {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "document_id", nullable = false)
  private Document document;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "guardian_id", nullable = false)
  private Guardian guardian;

  @Column(nullable = false)
  private String title;

  @Column(name = "page_count")
  private Integer pageCount;

  @Column(name = "learn_rate")
  private Integer learnRate;

  @Enumerated(EnumType.STRING)
  @Column(name = "convert_process_status", nullable = false)
  private ConvertProcessStatus convertProcessStatus;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "textbook", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<Page> pages = new ArrayList<>();

  @Builder
  public Textbook(Document document, Guardian guardian, String title, Integer pageCount, Integer learnRate) {
    this.document = document;
    this.guardian = guardian;
    this.title = title;
    this.pageCount = pageCount;
    this.learnRate = learnRate;
    this.convertProcessStatus = ConvertProcessStatus.PENDING;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public void setConvertProcessStatus(ConvertProcessStatus processStatus) {
    this.convertProcessStatus = processStatus;
  }

  public void setLearnRate(Integer learnRate) {
    this.learnRate = learnRate;
  }

  public void setPageCount(Integer pageCount) {
    this.pageCount = pageCount;
  }

  public void addPage(Page page) {
    this.pages.add(page);
    if (page.getTextbook() != this) {
      page.setTextbook(this);
    }
  }

  // 편의 메서드: Document ID 조회
  public Long getDocumentId() {
    return document != null ? document.getId() : null;
  }
}
