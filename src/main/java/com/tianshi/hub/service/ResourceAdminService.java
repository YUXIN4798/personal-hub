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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Set;
import java.util.Collections;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ResourceAdminService {

    private static final Logger log = LoggerFactory.getLogger(ResourceAdminService.class);
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
        String storedPath = null;
        try {
            storedPath = applyForm(resource, form);
            Resource saved = resourceRepository.save(resource);
            syncTags(saved, form.getTagIds());
            return saved;
        } catch (RuntimeException exception) {
            deleteFileQuietly(storedPath);
            throw exception;
        }
    }

    @Transactional
    public Resource update(Long id, ResourceForm form) {
        Resource resource = findResource(id);
        String oldFilePath = resource.getFilePath();
        String storedPath = null;
        try {
            storedPath = applyForm(resource, form);
            Resource saved = resourceRepository.save(resource);
            syncTags(saved, form.getTagIds());
            deleteAfterCommitIfReplaced(oldFilePath, saved.getFilePath());
            return saved;
        } catch (RuntimeException exception) {
            deleteFileQuietly(storedPath);
            throw exception;
        }
    }

    @Transactional
    public void delete(Long id) {
        Resource resource = findResource(id);
        String filePath = resource.getFilePath();
        resourceRepository.delete(resource);
        deleteAfterCommit(filePath);
    }

    @Transactional(readOnly = true)
    public ResourceForm toForm(Resource resource) {
        ResourceForm form = new ResourceForm();
        form.setTitle(resource.getTitle());
        form.setSlug(resource.getSlug());
        form.setSummary(resource.getSummary());
        form.setDescription(resource.getDescription());
        form.setUrl(resource.getUrl());
        form.setType(resource.getType());
        form.setCategoryId(resource.getCategoryId());
        form.setTagIds(findResourceTags(resource.getId()).stream().map(Tag::getId).toList());
        return form;
    }

    private String applyForm(Resource resource, ResourceForm form) {
        String storedPath = null;
        resource.setTitle(trim(form.getTitle()));
        resource.setSlug(trim(form.getSlug()));
        resource.setSummary(trim(form.getSummary()));
        resource.setDescription(trim(form.getDescription()));
        resource.setUrl(trim(form.getUrl()));
        resource.setType(normalizeType(form.getType()));
        resource.setCategoryId(form.getCategoryId());
        MultipartFile file = form.getFile();
        if (fileStorageService != null && file != null && !file.isEmpty()) {
            storedPath = fileStorageService.store(file, RESOURCE_UPLOAD_EXTENSIONS);
            try {
                resource.setFilePath(storedPath);
                resource.setOriginalName(extractFilename(file.getOriginalFilename()));
                resource.setFileSize(file.getSize());
                resource.setChecksum(calculateStoredFileSha256(storedPath));
                resource.setUrl(storedPath);
                resource.setType("file");
            } catch (RuntimeException exception) {
                deleteFileQuietly(storedPath);
                throw exception;
            }
        } else if ("file".equals(resource.getType())) {
            String filePath = trim(form.getUrl());
            if (filePath != null) {
                resource.setFilePath(filePath);
                applyExistingFileMetadata(resource, filePath);
            }
            if (resource.getOriginalName() == null) {
                resource.setOriginalName(resource.getTitle());
            }
        }
        if (resource.getVisibility() == null) {
            resource.setVisibility(DEFAULT_VISIBILITY);
        }
        if (resource.getVersion() == null) {
            resource.setVersion(DEFAULT_VERSION);
        }
        return storedPath;
    }

    private String extractFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return null;
        }
        return java.nio.file.Path.of(originalFilename).getFileName().toString();
    }

    private void applyExistingFileMetadata(Resource resource, String filePath) {
        if (fileStorageService == null) {
            return;
        }
        try {
            java.nio.file.Path resolvedPath = fileStorageService.resolve(filePath);
            if (!Files.isRegularFile(resolvedPath) || !Files.isReadable(resolvedPath)) {
                return;
            }
            resource.setFileSize(Files.size(resolvedPath));
            resource.setChecksum(calculateStoredFileSha256(filePath));
        } catch (RuntimeException | IOException exception) {
            log.warn("资源文件元数据读取失败: {}", filePath, exception);
        }
    }

    private String calculateStoredFileSha256(String storedPath) {
        try (InputStream inputStream = Files.newInputStream(fileStorageService.resolve(storedPath));
             DigestInputStream digestInputStream = new DigestInputStream(inputStream, MessageDigest.getInstance("SHA-256"))) {
            digestInputStream.transferTo(OutputStream.nullOutputStream());
            byte[] digest = digestInputStream.getMessageDigest().digest();
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("计算文件 checksum 失败", exception);
        }
    }

    private void deleteAfterCommitIfReplaced(String oldFilePath, String newFilePath) {
        if (oldFilePath == null || oldFilePath.equals(newFilePath)) {
            return;
        }
        deleteAfterCommit(oldFilePath);
    }

    private void deleteAfterCommit(String filePath) {
        if (fileStorageService == null || filePath == null || filePath.isBlank()) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deleteFileQuietly(filePath);
                }
            });
            return;
        }
        deleteFileQuietly(filePath);
    }

    private void deleteFileQuietly(String filePath) {
        if (fileStorageService == null || filePath == null || filePath.isBlank()) {
            return;
        }
        try {
            fileStorageService.delete(filePath);
        } catch (RuntimeException exception) {
            log.warn("资源文件清理失败: {}", filePath, exception);
        }
    }

    private void syncTags(Resource resource, List<Long> tagIds) {
        resourceTagRepository.deleteByResource_Id(resource.getId());
        List<Long> safeTagIds = tagIds == null ? Collections.emptyList() : tagIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
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
