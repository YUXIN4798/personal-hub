package com.tianshi.hub.service;

import com.tianshi.hub.dto.ProjectForm;
import com.tianshi.hub.entity.Category;
import com.tianshi.hub.entity.Project;
import com.tianshi.hub.entity.ProjectTag;
import com.tianshi.hub.entity.Tag;
import com.tianshi.hub.exception.ResourceNotFoundException;
import com.tianshi.hub.repository.CategoryRepository;
import com.tianshi.hub.repository.ProjectRepository;
import com.tianshi.hub.repository.ProjectTagRepository;
import com.tianshi.hub.repository.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class ProjectAdminService {

    private static final int PAGE_SIZE = 10;
    private static final String PROJECT_CATEGORY_TYPE = "project";

    private final ProjectRepository projectRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ProjectTagRepository projectTagRepository;

    public ProjectAdminService(
            ProjectRepository projectRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository,
            ProjectTagRepository projectTagRepository
    ) {
        this.projectRepository = projectRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.projectTagRepository = projectTagRepository;
    }

    @Transactional(readOnly = true)
    public Page<Project> findProjects(int page) {
        return projectRepository.findAll(PageRequest.of(Math.max(page, 0), PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "publishedAt").and(Sort.by(Sort.Direction.DESC, "id"))));
    }

    @Transactional(readOnly = true)
    public Project findProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在"));
    }

    @Transactional(readOnly = true)
    public List<Category> findProjectCategories() {
        return categoryRepository.findByTypeOrderBySortOrderAsc(PROJECT_CATEGORY_TYPE);
    }

    @Transactional(readOnly = true)
    public List<Tag> findAllTags() {
        return tagRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Tag> findProjectTags(Long projectId) {
        return projectTagRepository.findTagsByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public boolean slugExists(String slug, Long excludedId) {
        if (excludedId == null) {
            return projectRepository.existsBySlug(slug);
        }
        return projectRepository.existsBySlugAndIdNot(slug, excludedId);
    }

    @Transactional
    public Project create(ProjectForm form) {
        Project project = new Project();
        applyForm(project, form);
        Project saved = projectRepository.save(project);
        syncTags(saved, form.getTagIds());
        return saved;
    }

    @Transactional
    public Project update(Long id, ProjectForm form) {
        Project project = findProject(id);
        applyForm(project, form);
        Project saved = projectRepository.save(project);
        syncTags(saved, form.getTagIds());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        projectRepository.delete(findProject(id));
    }

    @Transactional(readOnly = true)
    public ProjectForm toForm(Project project) {
        ProjectForm form = new ProjectForm();
        form.setTitle(project.getTitle());
        form.setSlug(project.getSlug());
        form.setDescription(project.getDescription());
        form.setCoverUrl(project.getCoverImageUrl());
        form.setGithubUrl(project.getSourceUrl());
        form.setDemoUrl(project.getDemoUrl());
        form.setStatus(project.getStatus());
        form.setPublishedAt(project.getPublishedAt());
        form.setCategoryId(project.getCategoryId());
        form.setTagIds(findProjectTags(project.getId()).stream().map(Tag::getId).toList());
        return form;
    }

    private void applyForm(Project project, ProjectForm form) {
        project.setTitle(trim(form.getTitle()));
        project.setSlug(trim(form.getSlug()));
        project.setSummary(trimToLength(form.getDescription(), 500));
        project.setDescription(trim(form.getDescription()));
        project.setCoverImageUrl(trim(form.getCoverUrl()));
        project.setSourceUrl(trim(form.getGithubUrl()));
        project.setDemoUrl(trim(form.getDemoUrl()));
        project.setStatus(normalizeStatus(form.getStatus()));
        project.setPublishedAt(form.getPublishedAt());
        project.setCategoryId(form.getCategoryId());
        project.setFeatured(false);
    }

    private void syncTags(Project project, List<Long> tagIds) {
        projectTagRepository.deleteByProject_Id(project.getId());
        List<Long> safeTagIds = tagIds == null ? Collections.emptyList() : tagIds;
        tagRepository.findAllById(safeTagIds).stream()
                .map(tag -> new ProjectTag(project, tag))
                .forEach(projectTagRepository::save);
    }

    private String normalizeStatus(String status) {
        return "published".equals(status) ? "published" : "draft";
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String trimToLength(String value, int maxLength) {
        String trimmed = trim(value);
        if (trimmed == null || trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength);
    }
}
