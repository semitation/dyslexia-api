package com.dyslexia.dyslexia.entity;

import com.dyslexia.dyslexia.enums.DocumentProcessingStatus;
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
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor
public class Document {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "guardian_id", nullable = false)
  private Guardian guardian;

  @Column(nullable = false)
  private String title;

  @Column(name = "original_filename", nullable = false)
  private String originalFilename;

  @Setter
  @Column(name = "file_path", length = 500)
  private String filePath;

  @Column(name = "file_size")
  private Long fileSize;

  @Column(name = "mime_type", length = 100)
  private String mimeType;

  @Column(nullable = false)
  private LocalDateTime uploadedAt;

  @Column(columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String metadata;

  @Column(name = "job_id", unique = true, length = 36)
  private String jobId;

  @Enumerated(EnumType.STRING)
  @Column(name = "processing_status")
  private DocumentProcessingStatus processingStatus = DocumentProcessingStatus.PENDING;

  @Column(name = "progress")
  private Integer progress = 0;

  @Setter
  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Setter
  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Builder
  public Document(Guardian guardian, String title, String originalFilename, String filePath,
      Long fileSize, String mimeType, String metadata, String jobId) {
    this.guardian = guardian;
    this.title = title;
    this.originalFilename = originalFilename;
    this.filePath = filePath;
    this.fileSize = fileSize;
    this.mimeType = mimeType;
    this.metadata = metadata;
    this.jobId = jobId;
    this.uploadedAt = LocalDateTime.now();
  }

  @PreUpdate
  public void preUpdate() {
    this.uploadedAt = LocalDateTime.now();
  }

  public void setProcessingStatus(DocumentProcessingStatus status) {
    this.processingStatus = status;
    if (status == DocumentProcessingStatus.COMPLETED) {
      this.completedAt = LocalDateTime.now();
    }
  }

  public void setProgress(Integer progress) {
    this.progress = progress;
  }
}
