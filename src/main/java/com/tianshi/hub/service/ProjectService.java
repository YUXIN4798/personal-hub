package com.tianshi.hub.service;

import com.tianshi.hub.entity.Project;
import com.tianshi.hub.entity.Tag;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ProjectService {

    Page<Project> findPublishedProjects(int page, int size);

    Project findPublishedProjectBySlug(String slug);

    List<Tag> findProjectTags(Long projectId);

    Map<Long, List<Tag>> findProjectTags(List<Long> projectIds);
}
