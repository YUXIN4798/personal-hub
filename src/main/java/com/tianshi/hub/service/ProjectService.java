package com.tianshi.hub.service;

import com.tianshi.hub.entity.Project;
import org.springframework.data.domain.Page;

public interface ProjectService {

    Page<Project> findPublishedProjects(int page, int size);

    Project findPublishedProjectBySlug(String slug);
}
