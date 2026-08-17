package com.tianshi.hub.entity;

import java.io.Serializable;

public class ResourceTagId implements Serializable {

    private Long resource;
    private Long tag;

    public ResourceTagId() {
    }

    public ResourceTagId(Long resource, Long tag) {
        this.resource = resource;
        this.tag = tag;
    }

    public Long getResource() {
        return resource;
    }

    public Long getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ResourceTagId other)) {
            return false;
        }
        return resource.equals(other.resource) && tag.equals(other.tag);
    }

    @Override
    public int hashCode() {
        return 31 * resource.hashCode() + tag.hashCode();
    }
}

