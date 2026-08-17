package com.tianshi.hub.service;

import com.tianshi.hub.dto.ResourceForm;
import com.tianshi.hub.entity.Category;
import com.tianshi.hub.entity.Resource;
import com.tianshi.hub.entity.ResourceTag;
import com.tianshi.hub.entity.Tag;
import com.tianshi.hub.exception.ResourceNotFoundException;
import com.tianshi.hub.repository.CategoryRepository;
import com.tianshi.hub.repository.ResourceRepository;
import com.tianshi.hub.repository.ResourceTagRepository;
import com.tianshi.hub.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResourceAdminService {

    private static final int PAGE_SIZE = 10;
    private static final String RESOURCE_CATEGORY_TYPE = "resource";
    private static final String DEFAULT_VISIBILITY = "public";
    private static final String DEFAULT_VERSION = "v1.0";
    private static final Set<String> RESOURCE_UPLOAD_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "pdf", "zip", "rar", "7z", "txt", "md"
    );

    private final ResourceRepository resourceRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ResourceTagRepository resourceTagRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public ResourceAdminService(
            ResourceRepository resourceRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository,
            ResourceTagRepository resourceTagRepository,
            FileStorageService fileStorageService
    ) {
        this.resourceRepository = resourceRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.resourceTagRepository = resourceTagRepository;
        this.fileStorageService = fileStorageService;
    }

    public ResourceAdminService(
            ResourceRepository resourceRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository,
            ResourceTagRepository resourceTagRepository
    ) {
        this(resourceRepository, categoryRepository, tagRepository, resourceTagRepository, null);
    }

    @Transactional(readOnly = true)
    public Page<Resource> findResources(int page) {
        return resourceRepository.findAll(PageRequest.of(Math.max(page, 0), PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))));
    }

    @Transactional(readOnly = true)
    public Resource findResource(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("资源不存在"));
    }

    @Transactional(readOnly = true)
    public List<Category> findResourceCategories() {
        return categoryRepository.findByTypeOrderBySortOrderAsc(RESOURCE_CATEGORY_TYPE);
    }

    @Transactional(readOnly = true)
    public List<Tag> findAllTags() {
        return tagRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Tag> findResourceTags(Long resourceId) {
        return resourceTagRepository.findTagsByResourceId(resourceId);
    }

    @Transactional(readOnly = true)
    public Map<Long, String> findResourceTagNames(List<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return resourceTagRepository.findByResource_IdInOrderByTag_NameAsc(resourceIds).stream()
                .collect(Collectors.groupingBy(
                        resourceTag -> resourceTag.getResource().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(resourceTag -> resourceTag.getTag().getName(), Collectors.joining("、"))
                ));
    }

    @Transactional(readOnly = true)
    public boolean slugExists(String slug, Long excludedId) {
        if (excludedId == null) {
            return resourceRepository.existsBySlug(slug);
        }
        return resourceRepository.existsBySlugAndIdNot(slug, excludedId);
    }

    @Transactional
    public Resource create(ResourceForm form) {
        Resource resource = new Resource();
        resource.setVersion(DEFAULT_VERSION);
        resource.setDownloadCount(0);
        resource.setFileSize(0);
        resource.setVisibility(DEFAULT_VISIBILITY);
        applyForm(resource, form);
        Resource saved = resourceRepository.save(resource);
        syncTags(saved, form.getTagIds());
        return saved;
    }

    @Transactional
    public Resource update(Long id, ResourceForm form) {
        Resource resource = findResource(id);
        applyForm(resource, form);
        Resource saved = resourceRepository.save(resource);
        syncTags(saved, form.getTagIds());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        resourceRepository.delete(findResource(id));
    }

    @Transactional(readOnly = true)
    public ResourceForm toForm(Resource resource) {
        ResourceForm form = new ResourceForm();
        form.setTitle(resource.getTitle());
        form.setSlug(resource.getSlug());
        form.setSummary(resource.getSummary());
        form.setUrl(resource.getUrl());
        form.setType(resource.getType());
        form.setCategoryId(resource.getCategoryId());
        form.setTagIds(findResourceTags(resource.getId()).stream().map(Tag::getId).toList());
        return form;
    }

    private void applyForm(Resource resource, ResourceForm form) {
        resource.setTitle(trim(form.getTitle()));
        resource.setSlug(trim(form.getSlug()));
        resource.setSummary(trim(form.getSummary()));
        resource.setUrl(trim(form.getUrl()));
        resource.setType(normalizeType(form.getType()));
        resource.setCategoryId(form.getCategoryId());
        MultipartFile file = form.getFile();
        if (fileStorageService != null && file != null && !file.isEmpty()) {
            String storedPath = fileStorageService.store(file, RESOURCE_UPLOAD_EXTENSIONS);
            resource.setFilePath(storedPath);
            resource.setOriginalName(extractFilename(file.getOriginalFilename()));
            resource.setFileSize(file.getSize());
            resource.setUrl(storedPath);
            resource.setType("file");
        } else if ("file".equals(resource.getType())) {
            String filePath = trim(form.getUrl());
            if (filePath != null) {
                resource.setFilePath(filePath);
                if (resource.getFileSize() == 0) {
                    resource.setFileSize(0);
                }
            }
            if (resource.getOriginalName() == null) {
                resource.setOriginalName(resource.getTitle());
            }
        } else {
            resource.setFilePath(resource.getFilePath());
            resource.setOriginalName(resource.getOriginalName());
        }
        if (resource.getVisibility() == null) {
            resource.setVisibility(DEFAULT_VISIBILITY);
        }
        if (resource.getVersion() == null) {
            resource.setVersion(DEFAULT_VERSION);
        }
    }

    private String extractFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return null;
        }
        return java.nio.file.Path.of(originalFilename).getFileName().toString();
    }

    private void syncTags(Resource resource, List<Long> tagIds) {
        resourceTagRepository.deleteByResource_Id(resource.getId());
        List<Long> safeTagIds = tagIds == null ? Collections.emptyList() : tagIds;
        tagRepository.findAllById(safeTagIds).stream()
                .map(tag -> new ResourceTag(resource, tag))
                .forEach(resourceTagRepository::save);
    }

    private String normalizeType(String type) {
        return "file".equals(type) ? "file" : "link";
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
