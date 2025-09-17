package com.dyslexia.dyslexia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "document_results")
@Getter
@NoArgsConstructor
public class DocumentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", unique = true, nullable = false, length = 200)
    private String jobId;

    @Column(name = "pdf_name", nullable = false, length = 500)
    private String pdfName;

    @Column(name = "raw_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String rawData;

    @CreationTimestamp
    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    public DocumentResult(String jobId, String pdfName, String rawData) {
        this.jobId = jobId;
        this.pdfName = pdfName;
        this.rawData = rawData;
    }
}

