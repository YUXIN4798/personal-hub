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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public Map<Long, String> findProjectTagNames(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return projectTagRepository.findByProject_IdInOrderByTag_NameAsc(projectIds).stream()
                .collect(Collectors.groupingBy(
                        projectTag -> projectTag.getProject().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(projectTag -> projectTag.getTag().getName(), Collectors.joining("、"))
                ));
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
        form.setSummary(project.getSummary());
        form.setDescription(project.getDescription());
        form.setTechStack(project.getTechStack());
        form.setCoverUrl(project.getCoverImageUrl());
        form.setSourceUrl(project.getSourceUrl());
        form.setDemoUrl(project.getDemoUrl());
        form.setStatus(project.getStatus());
        form.setFeatured(project.isFeatured());
        form.setSortOrder(project.getSortOrder());
        form.setPublishedAt(project.getPublishedAt());
        form.setCategoryId(project.getCategoryId());
        form.setTagIds(findProjectTags(project.getId()).stream().map(Tag::getId).toList());
        return form;
    }

    private void applyForm(Project project, ProjectForm form) {
        project.setTitle(trim(form.getTitle()));
        project.setSlug(trim(form.getSlug()));
        project.setSummary(trim(form.getSummary()));
        project.setDescription(trim(form.getDescription()));
        project.setTechStack(trim(form.getTechStack()));
        project.setCoverImageUrl(trim(form.getCoverUrl()));
        project.setSourceUrl(trim(form.getSourceUrl()));
        project.setDemoUrl(trim(form.getDemoUrl()));
        project.setStatus(normalizeStatus(form.getStatus()));
        project.setFeatured(form.isFeatured());
        project.setSortOrder(normalizeSortOrder(form.getSortOrder()));
        project.setPublishedAt(form.getPublishedAt());
        project.setCategoryId(form.getCategoryId());
    }

    private void syncTags(Project project, List<Long> tagIds) {
        projectTagRepository.deleteByProject_Id(project.getId());
        List<Long> safeTagIds = tagIds == null ? Collections.emptyList() : tagIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
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

    private int normalizeSortOrder(Integer sortOrder) {
        return sortOrder == null || sortOrder < 0 ? 0 : sortOrder;
    }
}
