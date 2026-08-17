package com.tianshi.hub.service;

import com.tianshi.hub.entity.Category;
import com.tianshi.hub.entity.Post;
import com.tianshi.hub.entity.Tag;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PostService {

    Page<Post> findPublishedPosts(int page, int size);
    Post findPublishedPostBySlug(String slug);
    List<Category> findPostCategories();
    Category findPostCategory(Long id);
    List<Tag> findPostTags(Long postId);
}
