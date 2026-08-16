package com.tianshi.hub.service;

import com.tianshi.hub.entity.Category;
import com.tianshi.hub.entity.Resource;
import org.springframework.data.domain.Page;

import java.nio.file.Path;
import java.util.List;

public interface ResourceService {

    Page<Resource> findPublicResources(int page, int size, Long categoryId);

    List<Category> findResourceCategories();

    Resource findPublicResourceById(Long id);

    Category findCategoryById(Long id);

    ResourceDownload prepareDownload(Long id);

    record ResourceDownload(Resource resource, Path path) {
    }
}
