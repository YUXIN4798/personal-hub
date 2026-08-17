package com.tianshi.hub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class ResourceForm {

    @NotBlank(message = "标题不能为空")
    @Size(max = 128, message = "标题不能超过 128 个字符")
    private String title;

    @NotBlank(message = "Slug 不能为空")
    @Size(max = 128, message = "Slug 不能超过 128 个字符")
    private String slug;

    @Size(max = 500, message = "摘要不能超过 500 个字符")
    private String summary;

    @NotBlank(message = "URL 不能为空")
    @Size(max = 500, message = "URL 不能超过 500 个字符")
    private String url;

    @NotBlank(message = "类型不能为空")
    private String type = "link";

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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

