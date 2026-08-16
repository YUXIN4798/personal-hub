package com.tianshi.hub.entity;

import java.io.Serializable;

public class ProjectTagId implements Serializable {

    private Long project;
    private Long tag;

    public ProjectTagId() {
    }

    public ProjectTagId(Long project, Long tag) {
        this.project = project;
        this.tag = tag;
    }

    public Long getProject() {
        return project;
    }

    public Long getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ProjectTagId other)) {
            return false;
        }
        return project.equals(other.project) && tag.equals(other.tag);
    }

    @Override
    public int hashCode() {
        return 31 * project.hashCode() + tag.hashCode();
    }
}
