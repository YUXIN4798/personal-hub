package com.tianshi.hub.service;

import com.tianshi.hub.entity.Category;
import com.tianshi.hub.entity.Post;
import com.tianshi.hub.entity.Tag;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface PostService {

    Page<Post> findPublishedPosts(int page, int size);
    Post findPublishedPostBySlug(String slug);
    List<Category> findPostCategories();
    Category findPostCategory(Long id);
    List<Tag> findPostTags(Long postId);
    Map<Long, List<Tag>> findPostTags(List<Long> postIds);
}
