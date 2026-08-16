package com.tianshi.hub.service;

import com.tianshi.hub.entity.Project;
import com.tianshi.hub.exception.ResourceNotFoundException;
import com.tianshi.hub.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private static final String PUBLISHED_STATUS = "published";
    private static final int DEFAULT_PAGE_SIZE = 6;
    private static final int MAX_PAGE_SIZE = 24;

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public Page<Project> findPublishedProjects(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? Math.min(size, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
        PageRequest pageRequest = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.ASC, "sortOrder")
        );
        return projectRepository.findByStatus(PUBLISHED_STATUS, pageRequest);
    }

    @Override
    public Project findPublishedProjectBySlug(String slug) {
        return projectRepository.findBySlug(slug)
                .filter(project -> PUBLISHED_STATUS.equals(project.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在"));
    }
}
