package com.tianshi.hub.service;

import com.tianshi.hub.dto.CategoryForm;
import com.tianshi.hub.entity.Category;
import com.tianshi.hub.exception.ResourceNotFoundException;
import com.tianshi.hub.repository.CategoryRepository;
import com.tianshi.hub.repository.PostRepository;
import com.tianshi.hub.repository.ProjectRepository;
import com.tianshi.hub.repository.ResourceRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryAdminService {

    private static final Map<String, String> TYPE_LABELS = typeLabelsMap();

    private final CategoryRepository categoryRepository;
    private final ProjectRepository projectRepository;
    private final ResourceRepository resourceRepository;
    private final PostRepository postRepository;
    private final AdminSlugService slugService;

    public CategoryAdminService(
            CategoryRepository categoryRepository,
            ProjectRepository projectRepository,
            ResourceRepository resourceRepository,
            PostRepository postRepository,
            AdminSlugService slugService
    ) {
        this.categoryRepository = categoryRepository;
        this.projectRepository = projectRepository;
        this.resourceRepository = resourceRepository;
        this.postRepository = postRepository;
        this.slugService = slugService;
    }

    @Transactional(readOnly = true)
    public List<CategoryRow> findRows() {
        return categoryRepository.findAll(Sort.by("type").ascending().and(Sort.by("sortOrder").ascending())
                        .and(Sort.by("id").ascending()))
                .stream()
                .map(category -> new CategoryRow(category, usage(category.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));
    }

    @Transactional(readOnly = true)
    public boolean nameExists(String name, String type, Long excludedId) {
        String normalizedName = trim(name);
        String normalizedType = normalizeType(type);
        if (excludedId == null) {
            return categoryRepository.existsByNameAndType(normalizedName, normalizedType);
        }
        return categoryRepository.existsByNameAndTypeAndIdNot(normalizedName, normalizedType, excludedId);
    }

    @Transactional(readOnly = true)
    public boolean slugExists(String slug, String type, Long excludedId) {
        String normalizedSlug = resolveSlug(slug, "", "category");
        String normalizedType = normalizeType(type);
        if (excludedId == null) {
            return categoryRepository.existsBySlugAndType(normalizedSlug, normalizedType);
        }
        return categoryRepository.existsBySlugAndTypeAndIdNot(normalizedSlug, normalizedType, excludedId);
    }

    @Transactional
    public Category create(CategoryForm form) {
        Category category = new Category();
        category.setType(normalizeType(form.getType()));
        applyForm(category, form, category.getType());
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Long id, CategoryForm form) {
        Category category = findCategory(id);
        applyForm(category, form, category.getType());
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = findCategory(id);
        CategoryUsage usage = usage(category.getId());
        if (usage.total() > 0) {
            throw new IllegalStateException("该分类被 " + usage.total() + " 个内容使用，无法删除");
        }
        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public CategoryForm toForm(Category category) {
        CategoryForm form = new CategoryForm();
        form.setName(category.getName());
        form.setSlug(category.getSlug());
        form.setType(category.getType());
        form.setSortOrder(category.getSortOrder());
        return form;
    }

    public String resolveSlug(String slug, String name, String prefix) {
        return slugService.normalize(slug, name, prefix);
    }

    public boolean supportsType(String type) {
        return TYPE_LABELS.containsKey(type);
    }

    public Map<String, String> typeLabels() {
        return TYPE_LABELS;
    }

    public String labelForType(String type) {
        return TYPE_LABELS.getOrDefault(type, type);
    }

    private void applyForm(Category category, CategoryForm form, String existingType) {
        category.setName(trim(form.getName()));
        category.setSlug(resolveSlug(form.getSlug(), form.getName(), "category"));
        category.setType(existingType);
        category.setSortOrder(form.getSortOrder() == null || form.getSortOrder() < 0 ? 0 : form.getSortOrder());
    }

    private CategoryUsage usage(Long categoryId) {
        return new CategoryUsage(
                projectRepository.countByCategoryId(categoryId),
                resourceRepository.countByCategoryId(categoryId),
                postRepository.countByCategoryId(categoryId)
        );
    }

    private String normalizeType(String type) {
        String normalized = trim(type);
        if (!supportsType(normalized)) {
            throw new IllegalArgumentException("不支持的分类类型");
        }
        return normalized;
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }

    public record CategoryRow(Category category, CategoryUsage usage) {
    }

    public record CategoryUsage(long projects, long resources, long posts) {
        public long total() {
            return projects + resources + posts;
        }
    }

    private static Map<String, String> typeLabelsMap() {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("project", "项目");
        labels.put("resource", "资源");
        labels.put("post", "笔记");
        return Collections.unmodifiableMap(labels);
    }
}
