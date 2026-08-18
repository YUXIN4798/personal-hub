package com.tianshi.hub.service;

import com.tianshi.hub.entity.Category;
import com.tianshi.hub.entity.Resource;
import com.tianshi.hub.entity.ResourceTag;
import com.tianshi.hub.entity.Tag;
import com.tianshi.hub.exception.ResourceNotFoundException;
import com.tianshi.hub.repository.CategoryRepository;
import com.tianshi.hub.repository.ResourceRepository;
import com.tianshi.hub.repository.ResourceTagRepository;
import com.tianshi.hub.util.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResourceServiceImpl implements ResourceService {

    private static final String PUBLIC_VISIBILITY = "public";
    private static final String RESOURCE_CATEGORY_TYPE = "resource";
    private static final int DEFAULT_PAGE_SIZE = 6;
    private static final int MAX_PAGE_SIZE = 24;

    private final ResourceRepository resourceRepository;
    private final CategoryRepository categoryRepository;
    private final ResourceTagRepository resourceTagRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public ResourceServiceImpl(
            ResourceRepository resourceRepository,
            CategoryRepository categoryRepository,
            ResourceTagRepository resourceTagRepository,
            FileStorageService fileStorageService
    ) {
        this.resourceRepository = resourceRepository;
        this.categoryRepository = categoryRepository;
        this.resourceTagRepository = resourceTagRepository;
        this.fileStorageService = fileStorageService;
    }

    public ResourceServiceImpl(ResourceRepository resourceRepository, CategoryRepository categoryRepository) {
        this(resourceRepository, categoryRepository, null, null);
    }

    public ResourceServiceImpl(
            ResourceRepository resourceRepository,
            CategoryRepository categoryRepository,
            ResourceTagRepository resourceTagRepository
    ) {
        this(resourceRepository, categoryRepository, resourceTagRepository, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Resource> findPublicResources(int page, int size, Long categoryId) {
        PaginationUtil.PageBounds bounds = PaginationUtil.clamp(page, size, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);
        PageRequest pageRequest = PageRequest.of(
                bounds.page(),
                bounds.size(),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );
        if (categoryId == null) {
            return resourceRepository.findByVisibility(PUBLIC_VISIBILITY, pageRequest);
        }
        return resourceRepository.findByVisibilityAndCategoryId(PUBLIC_VISIBILITY, categoryId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findResourceCategories() {
        return categoryRepository.findByTypeOrderBySortOrderAsc(RESOURCE_CATEGORY_TYPE);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource findPublicResourceById(Long id) {
        return resourceRepository.findById(id)
                .filter(resource -> PUBLIC_VISIBILITY.equals(resource.getVisibility()))
                .orElseThrow(() -> new ResourceNotFoundException("资源不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public Resource findPublicResourceBySlug(String slug) {
        return resourceRepository.findBySlug(slug)
                .filter(resource -> PUBLIC_VISIBILITY.equals(resource.getVisibility()))
                .orElseThrow(() -> new ResourceNotFoundException("资源不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tag> findResourceTags(Long resourceId) {
        return resourceTagRepository.findTagsByResourceId(resourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<Tag>> findResourceTags(List<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return resourceTagRepository.findByResource_IdInOrderByTag_NameAsc(resourceIds).stream()
                .collect(Collectors.groupingBy(
                        resourceTag -> resourceTag.getResource().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(ResourceTag::getTag, Collectors.toList())
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDownloadAvailable(Resource resource) {
        if (resource == null) {
            return false;
        }
        String relativePath = resource.getFilePath() != null ? resource.getFilePath() : resource.getUrl();
        if (relativePath == null || relativePath.isBlank()) {
            return false;
        }
        try {
            Path path = fileStorageService != null ? fileStorageService.resolve(relativePath) : Path.of(relativePath).normalize();
            return java.nio.file.Files.isRegularFile(path) && java.nio.file.Files.isReadable(path);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    @Override
    @Transactional
    public ResourceDownload prepareDownload(Long id) {
        Resource resource = findPublicResourceById(id);
        String relativePath = resource.getFilePath() != null ? resource.getFilePath() : resource.getUrl();
        Path path = fileStorageService != null ? fileStorageService.resolve(relativePath) : Path.of(relativePath).normalize();
        if (!java.nio.file.Files.isRegularFile(path) || !java.nio.file.Files.isReadable(path)) {
            throw new ResourceNotFoundException("资源文件暂不可下载");
        }
        resourceRepository.incrementDownloadCount(id);
        return new ResourceDownload(resource, path);
    }
}
