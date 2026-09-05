package com.tianshi.hub.service;

import com.tianshi.hub.repository.PostRepository;
import com.tianshi.hub.repository.ProjectRepository;
import com.tianshi.hub.repository.ResourceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private static final int MAX_RESULTS_PER_GROUP = 20;

    private final ProjectRepository projectRepository;
    private final PostRepository postRepository;
    private final ResourceRepository resourceRepository;

    public SearchServiceImpl(
            ProjectRepository projectRepository,
            PostRepository postRepository,
            ResourceRepository resourceRepository
    ) {
        this.projectRepository = projectRepository;
        this.postRepository = postRepository;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public SearchResults search(String query) {
        String escapedQuery = escapeLike(query);
        PageRequest limit = PageRequest.of(0, MAX_RESULTS_PER_GROUP);
        return new SearchResults(
                projectRepository.search(escapedQuery, limit),
                postRepository.search(escapedQuery, limit),
                resourceRepository.search(escapedQuery, limit)
        );
    }

    private String escapeLike(String query) {
        return query
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
