package com.tianshi.hub.entity;

import java.io.Serializable;

public class PostTagId implements Serializable {

    private Long post;
    private Long tag;

    public PostTagId() {}

    public PostTagId(Long post, Long tag) {
        this.post = post;
        this.tag = tag;
    }

    public Long getPost() { return post; }
    public Long getTag() { return tag; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PostTagId other)) return false;
        return post.equals(other.post) && tag.equals(other.tag);
    }

    @Override
    public int hashCode() {
        return 31 * post.hashCode() + tag.hashCode();
    }
}
