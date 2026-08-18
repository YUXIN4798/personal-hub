package com.tianshi.hub.dto;

public class SiteConfigForm {

    private String heroTitle;
    private String heroSubtitle;
    private String projectsCount;
    private String notesCount;
    private String resourcesCount;

    public String getHeroTitle() {
        return heroTitle;
    }

    public void setHeroTitle(String heroTitle) {
        this.heroTitle = heroTitle;
    }

    public String getHeroSubtitle() {
        return heroSubtitle;
    }

    public void setHeroSubtitle(String heroSubtitle) {
        this.heroSubtitle = heroSubtitle;
    }

    public String getProjectsCount() {
        return projectsCount;
    }

    public void setProjectsCount(String projectsCount) {
        this.projectsCount = projectsCount;
    }

    public String getNotesCount() {
        return notesCount;
    }

    public void setNotesCount(String notesCount) {
        this.notesCount = notesCount;
    }

    public String getResourcesCount() {
        return resourcesCount;
    }

    public void setResourcesCount(String resourcesCount) {
        this.resourcesCount = resourcesCount;
    }
}
