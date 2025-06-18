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
  @JoinColumn(name = "teacher_id", nullable = false)
  private Teacher teacher;

  @Column(nullable = false)
  private String title;

  @Column(name = "original_filename", nullable = false)
  private String originalFilename;

  @Setter
  @Column(name = "file_path", nullable = true, length = 500)
  private String filePath;

  @Column(name = "file_size")
  private Long fileSize;

  @Column(name = "mime_type", length = 100)
  private String mimeType;

  private String state;
  private String type;

  @Column(nullable = false)
  private LocalDateTime uploadedAt;

  @Column(columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String metadata;

  @Builder
  public Document(Teacher teacher, String title, String originalFilename, String filePath,
      Long fileSize, String mimeType, String state, String type, String metadata) {
    this.teacher = teacher;
    this.title = title;
    this.originalFilename = originalFilename;
    this.filePath = filePath;
    this.fileSize = fileSize;
    this.mimeType = mimeType;
    this.state = state;
    this.type = type;
    this.metadata = metadata;
    this.uploadedAt = LocalDateTime.now();
  }

  @PreUpdate
  public void preUpdate() {
    this.uploadedAt = LocalDateTime.now();
  }
}
