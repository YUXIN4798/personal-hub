package com.tianshi.hub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TagForm {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 64, message = "标签名称不能超过 64 个字符")
    private String name;

    @Size(max = 64, message = "Slug 不能超过 64 个字符")
    private String slug;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
