package com.jodak.admin.entities;

import com.jodak.admin.enums.DuplicateStrategy;
import com.jodak.admin.enums.ImportFormat;
import com.jodak.admin.enums.ImportJobType;
import com.jodak.admin.enums.ImportMode;
import com.jodak.admin.enums.ImportStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Job d'import asynchrone. Mis à jour au fil du traitement (progression, compteurs, statut).
 */
@Entity
@Table(name = "import_job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 30)
    private ImportJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 10)
    private ImportFormat format;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 10)
    private ImportMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "duplicate_strategy", nullable = false, length = 10)
    private DuplicateStrategy duplicateStrategy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    private ImportStatus status;

    @Column(name = "source_file_name", nullable = false, length = 255)
    private String sourceFileName;

    @Column(name = "stored_file_path", nullable = false, length = 500)
    private String storedFilePath;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "total_rows", nullable = false)
    private long totalRows;

    @Column(name = "processed_rows", nullable = false)
    private long processedRows;

    @Column(name = "imported_rows", nullable = false)
    private long importedRows;

    @Column(name = "updated_rows", nullable = false)
    private long updatedRows;

    @Column(name = "skipped_rows", nullable = false)
    private long skippedRows;

    @Column(name = "failed_rows", nullable = false)
    private long failedRows;

    @Column(name = "progress_percent", nullable = false)
    private int progressPercent;

    @Column(name = "cancel_requested", nullable = false)
    private boolean cancelRequested;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "correlation_id", nullable = false, length = 36)
    private String correlationId;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public boolean isTerminal() {
        return status == ImportStatus.COMPLETED
                || status == ImportStatus.FAILED
                || status == ImportStatus.CANCELLED;
    }
}
