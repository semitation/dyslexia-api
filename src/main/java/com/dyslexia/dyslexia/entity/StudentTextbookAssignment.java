package com.dyslexia.dyslexia.entity;

import com.dyslexia.dyslexia.enums.CompletionStatus;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student_textbook_assignments", uniqueConstraints = @UniqueConstraint(columnNames = {
    "student_id", "textbook_id"}))
@Getter
@NoArgsConstructor
public class StudentTextbookAssignment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "textbook_id", nullable = false)
  private Textbook textbook;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_by", nullable = false)
  private Guardian assignedBy;

  @Column(name = "current_page_number")
  private Integer currentPageNumber = 1;

  @Column(name = "assigned_at", nullable = false)
  private LocalDateTime assignedAt;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @Enumerated(EnumType.STRING)
  @Column(name = "completion_status", nullable = false)
  private CompletionStatus completionStatus = CompletionStatus.NOT_STARTED;

  @Column(name = "total_learning_time")
  private Integer totalLearningTime = 0;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Builder
  public StudentTextbookAssignment(Student student, Textbook textbook, Guardian assignedBy,
      Integer currentPageNumber, CompletionStatus completionStatus, Integer totalLearningTime,
      LocalDateTime assignedAt, String notes) {
    this.student = student;
    this.textbook = textbook;
    this.assignedBy = assignedBy;
    this.currentPageNumber = currentPageNumber != null ? currentPageNumber : 1;
    this.assignedAt = assignedAt != null ? assignedAt : LocalDateTime.now();
    this.notes = notes;
    this.completionStatus =
        completionStatus != null ? completionStatus : CompletionStatus.NOT_STARTED;
    this.totalLearningTime = totalLearningTime != null ? totalLearningTime : 0;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public void updateProgress(Integer currentPageNumber, CompletionStatus completionStatus,
      Integer additionalLearningTime) {
    if (currentPageNumber != null && currentPageNumber > this.currentPageNumber) {
      this.currentPageNumber = currentPageNumber;
    }

    if (completionStatus != null) {
      this.completionStatus = completionStatus;
    }

    if (additionalLearningTime != null && additionalLearningTime > 0) {
      this.totalLearningTime += additionalLearningTime;
    }
  }

  public Guardian getGuardian() {
    return this.assignedBy;
  }
} 