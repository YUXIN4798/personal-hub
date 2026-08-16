package com.tianshi.hub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "resources")
@EntityListeners(AuditingEntityListener.class)
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(nullable = false, unique = true, length = 128)
    private String slug;

    @Column(length = 500)
    private String summary;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false, length = 32)
    private String type;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "original_name", length = 255)
    private String originalName;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(nullable = false, length = 32)
    private String version;

    @Column(name = "download_count", nullable = false)
    private long downloadCount;

    @Column(nullable = false, length = 32)
    private String visibility;

    @Column(columnDefinition = "CHAR(64)")
    private String checksum;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getOriginalName() {
        return originalName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getVersion() {
        return version;
    }

    public long getDownloadCount() {
        return downloadCount;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getChecksum() {
        return checksum;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void incrementDownloadCount() {
        this.downloadCount++;
    }
}
