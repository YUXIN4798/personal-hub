package com.tianshi.hub.service;

import com.tianshi.hub.entity.Category;
import com.tianshi.hub.entity.Resource;
import com.tianshi.hub.exception.ResourceNotFoundException;
import com.tianshi.hub.repository.CategoryRepository;
import com.tianshi.hub.repository.ResourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class ResourceServiceImpl implements ResourceService {

    private static final String PUBLIC_VISIBILITY = "public";
    private static final String RESOURCE_CATEGORY_TYPE = "resource";
    private static final int DEFAULT_PAGE_SIZE = 6;
    private static final int MAX_PAGE_SIZE = 24;

    private final ResourceRepository resourceRepository;
    private final CategoryRepository categoryRepository;

    public ResourceServiceImpl(ResourceRepository resourceRepository, CategoryRepository categoryRepository) {
        this.resourceRepository = resourceRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Resource> findPublicResources(int page, int size, Long categoryId) {
        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? Math.min(size, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
        PageRequest pageRequest = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
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
    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));
    }

    @Override
    @Transactional
    public ResourceDownload prepareDownload(Long id) {
        Resource resource = findPublicResourceById(id);
        Path path = Path.of(resource.getFilePath()).normalize();
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new ResourceNotFoundException("资源文件暂不可下载");
        }
        resource.incrementDownloadCount();
        return new ResourceDownload(resource, path);
    }
}
