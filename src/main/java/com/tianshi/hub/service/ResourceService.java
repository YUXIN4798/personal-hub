package com.tianshi.hub.service;

import com.tianshi.hub.entity.Category;
import com.tianshi.hub.entity.Resource;
import com.tianshi.hub.entity.Tag;
import org.springframework.data.domain.Page;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface ResourceService {

    Page<Resource> findPublicResources(int page, int size, Long categoryId);

    List<Category> findResourceCategories();

    Resource findPublicResourceById(Long id);

    Resource findPublicResourceBySlug(String slug);

    Category findCategoryById(Long id);

    List<Tag> findResourceTags(Long resourceId);

    Map<Long, List<Tag>> findResourceTags(List<Long> resourceIds);

    boolean isDownloadAvailable(Resource resource);

    ResourceDownload prepareDownload(Long id);

    record ResourceDownload(Resource resource, Path path) {
    }
}
