package com.tianshi.hub.service;

import com.tianshi.hub.dto.PostForm;
import com.tianshi.hub.entity.Category;
import com.tianshi.hub.entity.Post;
import com.tianshi.hub.entity.PostTag;
import com.tianshi.hub.entity.Tag;
import com.tianshi.hub.exception.ResourceNotFoundException;
import com.tianshi.hub.repository.CategoryRepository;
import com.tianshi.hub.repository.PostRepository;
import com.tianshi.hub.repository.PostTagRepository;
import com.tianshi.hub.repository.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PostAdminService {

    private static final int PAGE_SIZE = 10;
    private static final String POST_CATEGORY_TYPE = "post";

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    public PostAdminService(
            PostRepository postRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository,
            PostTagRepository postTagRepository
    ) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.postTagRepository = postTagRepository;
    }

    @Transactional(readOnly = true)
    public Page<Post> findPosts(int page) {
        return postRepository.findAll(PageRequest.of(Math.max(page, 0), PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "publishedAt").and(Sort.by(Sort.Direction.DESC, "id"))));
    }

    @Transactional(readOnly = true)
    public Post findPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("笔记不存在"));
    }

    @Transactional(readOnly = true)
    public List<Category> findPostCategories() {
        return categoryRepository.findByTypeOrderBySortOrderAsc(POST_CATEGORY_TYPE);
    }

    @Transactional(readOnly = true)
    public List<Tag> findAllTags() {
        return tagRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Tag> findPostTags(Long postId) {
        return postTagRepository.findTagsByPostId(postId);
    }

    @Transactional(readOnly = true)
    public Map<Long, String> findPostTagNames(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return postTagRepository.findByPost_IdInOrderByTag_NameAsc(postIds).stream()
                .collect(Collectors.groupingBy(
                        postTag -> postTag.getPost().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(postTag -> postTag.getTag().getName(), Collectors.joining("、"))
                ));
    }

    @Transactional(readOnly = true)
    public boolean slugExists(String slug, Long excludedId) {
        if (excludedId == null) {
            return postRepository.existsBySlug(slug);
        }
        return postRepository.existsBySlugAndIdNot(slug, excludedId);
    }

    @Transactional
    public Post create(PostForm form) {
        Post post = new Post();
        applyForm(post, form);
        Post saved = postRepository.save(post);
        syncTags(saved, form.getTagIds());
        return saved;
    }

    @Transactional
    public Post update(Long id, PostForm form) {
        Post post = findPost(id);
        applyForm(post, form);
        Post saved = postRepository.save(post);
        syncTags(saved, form.getTagIds());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        postRepository.delete(findPost(id));
    }

    @Transactional(readOnly = true)
    public PostForm toForm(Post post) {
        PostForm form = new PostForm();
        form.setTitle(post.getTitle());
        form.setSlug(post.getSlug());
        form.setSummary(post.getSummary());
        form.setContent(post.getContent());
        form.setStatus(post.getStatus());
        form.setPublishedAt(post.getPublishedAt());
        form.setCategoryId(post.getCategoryId());
        form.setTagIds(findPostTags(post.getId()).stream().map(Tag::getId).toList());
        return form;
    }

    private void applyForm(Post post, PostForm form) {
        post.setTitle(trim(form.getTitle()));
        post.setSlug(trim(form.getSlug()));
        post.setSummary(trim(form.getSummary()));
        post.setContent(form.getContent() == null ? null : form.getContent().trim());
        post.setStatus("published".equals(form.getStatus()) ? "published" : "draft");
        post.setPublishedAt(form.getPublishedAt());
        post.setCategoryId(form.getCategoryId());
    }

    private void syncTags(Post post, List<Long> tagIds) {
        postTagRepository.deleteByPost_Id(post.getId());
        List<Long> safeTagIds = tagIds == null ? Collections.emptyList() : tagIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        tagRepository.findAllById(safeTagIds).stream()
                .map(tag -> new PostTag(post, tag))
                .forEach(postTagRepository::save);
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
