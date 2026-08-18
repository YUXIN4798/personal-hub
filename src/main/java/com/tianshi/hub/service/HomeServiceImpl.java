package com.tianshi.hub.service;

import com.tianshi.hub.dto.HomeContent;
import com.tianshi.hub.entity.Post;
import com.tianshi.hub.entity.Project;
import com.tianshi.hub.entity.Resource;
import com.tianshi.hub.repository.PostRepository;
import com.tianshi.hub.repository.ProjectRepository;
import com.tianshi.hub.repository.ResourceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class HomeServiceImpl implements HomeService {

    private static final String PUBLISHED_STATUS = "published";
    private static final String PUBLIC_VISIBILITY = "public";
    private static final int SUMMARY_LIMIT = 80;

    private final ProjectRepository projectRepository;
    private final PostRepository postRepository;
    private final ResourceRepository resourceRepository;
    private final SiteConfigService siteConfigService;

    public HomeServiceImpl(
            ProjectRepository projectRepository,
            PostRepository postRepository,
            ResourceRepository resourceRepository,
            SiteConfigService siteConfigService
    ) {
        this.projectRepository = projectRepository;
        this.postRepository = postRepository;
        this.resourceRepository = resourceRepository;
        this.siteConfigService = siteConfigService;
    }

    @Override
    public HomeContent getHomeContent() {
        int projectCount = sectionCount(SiteConfigKeys.PROJECTS_COUNT);
        int noteCount = sectionCount(SiteConfigKeys.NOTES_COUNT);
        int resourceCount = sectionCount(SiteConfigKeys.RESOURCES_COUNT);

        return new HomeContent(
                siteConfigService.get(SiteConfigKeys.HERO_TITLE, SiteConfigKeys.DEFAULT_HERO_TITLE),
                siteConfigService.get(SiteConfigKeys.HERO_SUBTITLE, SiteConfigKeys.DEFAULT_HERO_SUBTITLE),
                findProjects(projectCount),
                findNotes(noteCount),
                findResources(resourceCount)
        );
    }

    private List<HomeContent.ProjectItem> findProjects(int count) {
        if (count <= 0) {
            return List.of();
        }
        PageRequest featuredRequest = PageRequest.of(0, count,
                Sort.by(Sort.Direction.ASC, "sortOrder")
                        .and(Sort.by(Sort.Direction.DESC, "createdAt"))
                        .and(Sort.by(Sort.Direction.DESC, "id")));
        List<Project> projects = new ArrayList<>(projectRepository
                .findByStatusAndFeatured(PUBLISHED_STATUS, true, featuredRequest)
                .getContent());
        if (projects.size() < count) {
            List<Long> selectedIds = projects.stream().map(Project::getId).toList();
            PageRequest latestRequest = PageRequest.of(0, count - projects.size(),
                    Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
            List<Project> fallbackProjects = selectedIds.isEmpty()
                    ? projectRepository.findByStatus(PUBLISHED_STATUS, latestRequest).getContent()
                    : projectRepository.findByStatusAndIdNotIn(PUBLISHED_STATUS, selectedIds, latestRequest).getContent();
            projects.addAll(fallbackProjects);
        }
        return projects.stream()
                .limit(count)
                .map(project -> new HomeContent.ProjectItem(
                        project.getTitle(),
                        project.getSlug(),
                        truncate(project.getSummary(), SUMMARY_LIMIT),
                        firstNonNull(project.getPublishedAt(), project.getCreatedAt())
                ))
                .toList();
    }

    private List<HomeContent.NoteItem> findNotes(int count) {
        if (count <= 0) {
            return List.of();
        }
        PageRequest pageRequest = PageRequest.of(0, count,
                Sort.by(Sort.Direction.DESC, "publishedAt").and(Sort.by(Sort.Direction.DESC, "id")));
        return postRepository.findByStatus(PUBLISHED_STATUS, pageRequest).stream()
                .map(post -> new HomeContent.NoteItem(post.getTitle(), post.getSlug(), post.getPublishedAt()))
                .toList();
    }

    private List<HomeContent.ResourceItem> findResources(int count) {
        if (count <= 0) {
            return List.of();
        }
        PageRequest pageRequest = PageRequest.of(0, count,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
        return resourceRepository.findByVisibility(PUBLIC_VISIBILITY, pageRequest).stream()
                .map(resource -> new HomeContent.ResourceItem(resource.getTitle(), resource.getSlug(), resource.getType()))
                .toList();
    }

    private int sectionCount(String key) {
        int value = siteConfigService.getInt(key, SiteConfigKeys.DEFAULT_SECTION_COUNT);
        return value < 0 ? SiteConfigKeys.DEFAULT_SECTION_COUNT : value;
    }

    private String truncate(String value, int limit) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        int length = trimmed.codePointCount(0, trimmed.length());
        if (length <= limit) {
            return trimmed;
        }
        int end = trimmed.offsetByCodePoints(0, limit);
        return trimmed.substring(0, end) + "...";
    }

    private LocalDateTime firstNonNull(LocalDateTime first, LocalDateTime second) {
        return first != null ? first : second;
    }
}
