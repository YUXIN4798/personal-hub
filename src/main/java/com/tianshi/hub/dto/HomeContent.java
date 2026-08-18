package com.tianshi.hub.dto;

import java.time.LocalDateTime;
import java.util.List;

public record HomeContent(
        String heroTitle,
        String heroSubtitle,
        List<ProjectItem> projects,
        List<NoteItem> notes,
        List<ResourceItem> resources
) {

    public record ProjectItem(String title, String slug, String summary, LocalDateTime date) {
    }

    public record NoteItem(String title, String slug, LocalDateTime date) {
    }

    public record ResourceItem(String title, String slug, String type) {
    }
}
