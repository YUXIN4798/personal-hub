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
    public boolean heroTitleHasAccent() {
        return heroTitleLastLineBreak() >= 0 && !heroTitleAccent().isEmpty();
    }

    public String heroTitlePrefix() {
        int lineBreak = heroTitleLastLineBreak();
        if (lineBreak < 0) {
            return safeHeroTitle();
        }
        return safeHeroTitle().substring(0, lineBreak + 1);
    }

    public String heroTitleAccent() {
        int lineBreak = heroTitleLastLineBreak();
        if (lineBreak < 0) {
            return "";
        }
        return safeHeroTitle().substring(lineBreak + 1);
    }

    private int heroTitleLastLineBreak() {
        return safeHeroTitle().lastIndexOf('\n');
    }

    private String safeHeroTitle() {
        return heroTitle == null ? "" : heroTitle.replace("\r\n", "\n").replace('\r', '\n');
    }

    public record ProjectItem(String title, String slug, String summary, LocalDateTime date) {
    }

    public record NoteItem(String title, String slug, LocalDateTime date) {
    }

    public record ResourceItem(String title, String slug, String type) {
    }
}
