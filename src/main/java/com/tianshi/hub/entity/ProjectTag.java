package com.tianshi.hub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_tags")
@IdClass(ProjectTagId.class)
public class ProjectTag {

    @Id
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Id
    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public ProjectTag() {
    }

    public ProjectTag(Project project, Tag tag) {
        this.project = project;
        this.tag = tag;
    }

    public Project getProject() {
        return project;
    }

    public Tag getTag() {
        return tag;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
