package com.tianshi.hub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProjectForm {

    @NotBlank(message = "标题不能为空")
    @Size(max = 128, message = "标题不能超过 128 个字符")
    private String title;

    @NotBlank(message = "Slug 不能为空")
    @Size(max = 128, message = "Slug 不能超过 128 个字符")
    private String slug;

    @Size(max = 2000, message = "描述不能超过 2000 个字符")
    private String description;

    @Size(max = 500, message = "摘要不能超过 500 个字符")
    private String summary;

    @Size(max = 500, message = "技术栈不能超过 500 个字符")
    private String techStack;

    @Size(max = 500, message = "封面 URL 不能超过 500 个字符")
    @Pattern(regexp = "(?i)^$|^(https?://|/(?!/)).+", message = "封面 URL 只能使用 http://、https:// 或站内 / 开头路径")
    private String coverUrl;

    @Size(max = 500, message = "源码 URL 不能超过 500 个字符")
    @Pattern(regexp = "(?i)^$|^(https?://|/(?!/)).+", message = "源码 URL 只能使用 http://、https:// 或站内 / 开头路径")
    private String sourceUrl;

    @Size(max = 500, message = "演示 URL 不能超过 500 个字符")
    @Pattern(regexp = "(?i)^$|^(https?://|/(?!/)).+", message = "演示 URL 只能使用 http://、https:// 或站内 / 开头路径")
    private String demoUrl;

    @NotBlank(message = "状态不能为空")
    private String status = "draft";

    private boolean featured;

    @Min(value = 0, message = "排序号不能小于 0")
    private Integer sortOrder = 0;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime publishedAt;

    private Long categoryId;

    private List<Long> tagIds = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTechStack() {
        return techStack;
    }

    public void setTechStack(String techStack) {
        this.techStack = techStack;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getDemoUrl() {
        return demoUrl;
    }

    public void setDemoUrl(String demoUrl) {
        this.demoUrl = demoUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds == null ? new ArrayList<>() : tagIds;
    }
}
