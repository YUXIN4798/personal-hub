package com.tianshi.hub.service;

import com.tianshi.hub.entity.Project;
import com.tianshi.hub.entity.ProjectTag;
import com.tianshi.hub.entity.Tag;
import com.tianshi.hub.exception.ResourceNotFoundException;
import com.tianshi.hub.repository.ProjectRepository;
import com.tianshi.hub.repository.ProjectTagRepository;
import com.tianshi.hub.util.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private static final String PUBLISHED_STATUS = "published";
    private static final int DEFAULT_PAGE_SIZE = 6;
    private static final int MAX_PAGE_SIZE = 24;

    private final ProjectRepository projectRepository;
    private final ProjectTagRepository projectTagRepository;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectTagRepository projectTagRepository) {
        this.projectRepository = projectRepository;
        this.projectTagRepository = projectTagRepository;
    }

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this(projectRepository, null);
    }

    @Override
    public Page<Project> findPublishedProjects(int page, int size) {
        PaginationUtil.PageBounds bounds = PaginationUtil.clamp(page, size, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);
        PageRequest pageRequest = PageRequest.of(
                bounds.page(),
                bounds.size(),
                Sort.by(Sort.Direction.ASC, "sortOrder").and(Sort.by(Sort.Direction.DESC, "id"))
        );
        return projectRepository.findByStatus(PUBLISHED_STATUS, pageRequest);
    }

    @Override
    public Project findPublishedProjectBySlug(String slug) {
        return projectRepository.findBySlug(slug)
                .filter(project -> PUBLISHED_STATUS.equals(project.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在"));
    }

    @Override
    public List<Tag> findProjectTags(Long projectId) {
        return projectTagRepository.findTagsByProjectId(projectId);
    }

    @Override
    public Map<Long, List<Tag>> findProjectTags(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return projectTagRepository.findByProject_IdInOrderByTag_NameAsc(projectIds).stream()
                .collect(Collectors.groupingBy(
                        projectTag -> projectTag.getProject().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(ProjectTag::getTag, Collectors.toList())
                ));
    }
}
