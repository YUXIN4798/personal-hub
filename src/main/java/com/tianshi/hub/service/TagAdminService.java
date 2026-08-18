package com.tianshi.hub.service;

import com.tianshi.hub.dto.TagForm;
import com.tianshi.hub.entity.Tag;
import com.tianshi.hub.exception.ResourceNotFoundException;
import com.tianshi.hub.repository.PostTagRepository;
import com.tianshi.hub.repository.ProjectTagRepository;
import com.tianshi.hub.repository.ResourceTagRepository;
import com.tianshi.hub.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagAdminService {

    private final TagRepository tagRepository;
    private final ProjectTagRepository projectTagRepository;
    private final ResourceTagRepository resourceTagRepository;
    private final PostTagRepository postTagRepository;
    private final AdminSlugService slugService;

    public TagAdminService(
            TagRepository tagRepository,
            ProjectTagRepository projectTagRepository,
            ResourceTagRepository resourceTagRepository,
            PostTagRepository postTagRepository,
            AdminSlugService slugService
    ) {
        this.tagRepository = tagRepository;
        this.projectTagRepository = projectTagRepository;
        this.resourceTagRepository = resourceTagRepository;
        this.postTagRepository = postTagRepository;
        this.slugService = slugService;
    }

    @Transactional(readOnly = true)
    public List<TagRow> findRows() {
        return tagRepository.findAllByOrderByNameAsc().stream()
                .map(tag -> new TagRow(tag, usage(tag.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Tag findTag(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在"));
    }

    @Transactional(readOnly = true)
    public boolean nameExists(String name, Long excludedId) {
        String normalizedName = trim(name);
        if (excludedId == null) {
            return tagRepository.existsByName(normalizedName);
        }
        return tagRepository.existsByNameAndIdNot(normalizedName, excludedId);
    }

    @Transactional(readOnly = true)
    public boolean slugExists(String slug, Long excludedId) {
        String normalizedSlug = resolveSlug(slug, "", "tag");
        if (excludedId == null) {
            return tagRepository.existsBySlug(normalizedSlug);
        }
        return tagRepository.existsBySlugAndIdNot(normalizedSlug, excludedId);
    }

    @Transactional
    public Tag create(TagForm form) {
        Tag tag = new Tag();
        applyForm(tag, form);
        return tagRepository.save(tag);
    }

    @Transactional
    public Tag update(Long id, TagForm form) {
        Tag tag = findTag(id);
        applyForm(tag, form);
        return tagRepository.save(tag);
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = findTag(id);
        TagUsage usage = usage(tag.getId());
        if (usage.total() > 0) {
            throw new IllegalStateException("该标签已被内容使用，请先解绑后再删除");
        }
        tagRepository.delete(tag);
    }

    @Transactional(readOnly = true)
    public TagForm toForm(Tag tag) {
        TagForm form = new TagForm();
        form.setName(tag.getName());
        form.setSlug(tag.getSlug());
        return form;
    }

    public String resolveSlug(String slug, String name, String prefix) {
        return slugService.normalize(slug, name, prefix);
    }

    private void applyForm(Tag tag, TagForm form) {
        tag.setName(trim(form.getName()));
        tag.setSlug(resolveSlug(form.getSlug(), form.getName(), "tag"));
    }

    private TagUsage usage(Long tagId) {
        return new TagUsage(
                projectTagRepository.countByTag_Id(tagId),
                resourceTagRepository.countByTag_Id(tagId),
                postTagRepository.countByTag_Id(tagId)
        );
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }

    public record TagRow(Tag tag, TagUsage usage) {
    }

    public record TagUsage(long projects, long resources, long posts) {
        public long total() {
            return projects + resources + posts;
        }
    }
}
