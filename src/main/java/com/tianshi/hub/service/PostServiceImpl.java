package com.tianshi.hub.service;

import com.tianshi.hub.entity.Category;
import com.tianshi.hub.entity.Post;
import com.tianshi.hub.entity.Tag;
import com.tianshi.hub.exception.ResourceNotFoundException;
import com.tianshi.hub.repository.CategoryRepository;
import com.tianshi.hub.repository.PostRepository;
import com.tianshi.hub.repository.PostTagRepository;
import com.tianshi.hub.util.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private static final String PUBLISHED_STATUS = "published";
    private static final int DEFAULT_PAGE_SIZE = 6;
    private static final int MAX_PAGE_SIZE = 24;

    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;
    private final CategoryRepository categoryRepository;

    public PostServiceImpl(
            PostRepository postRepository,
            PostTagRepository postTagRepository,
            CategoryRepository categoryRepository
    ) {
        this.postRepository = postRepository;
        this.postTagRepository = postTagRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Page<Post> findPublishedPosts(int page, int size) {
        PaginationUtil.PageBounds bounds = PaginationUtil.clamp(page, size, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);
        return postRepository.findByStatus(PUBLISHED_STATUS, PageRequest.of(
                bounds.page(), bounds.size(),
                Sort.by(Sort.Direction.DESC, "publishedAt").and(Sort.by(Sort.Direction.DESC, "id"))
        ));
    }

    @Override
    public Post findPublishedPostBySlug(String slug) {
        return postRepository.findBySlug(slug)
                .filter(post -> PUBLISHED_STATUS.equals(post.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("笔记不存在"));
    }

    public List<Tag> findPostTags(Long postId) {
        return postTagRepository.findTagsByPostId(postId);
    }

    @Override
    public Map<Long, List<Tag>> findPostTags(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return postTagRepository.findByPost_IdInOrderByTag_NameAsc(postIds).stream()
                .collect(Collectors.groupingBy(
                        postTag -> postTag.getPost().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(postTag -> postTag.getTag(), Collectors.toList())
                ));
    }

    @Override
    public List<Category> findPostCategories() {
        return categoryRepository.findByTypeOrderBySortOrderAsc("post");
    }

    @Override
    public Category findPostCategory(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }
}
