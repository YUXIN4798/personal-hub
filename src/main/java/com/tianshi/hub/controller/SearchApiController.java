package com.tianshi.hub.controller;

import com.tianshi.hub.entity.Post;
import com.tianshi.hub.entity.Project;
import com.tianshi.hub.entity.Resource;
import com.tianshi.hub.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class SearchApiController {

    private static final int MAX_QUERY_LENGTH = 50;

    private final SearchService searchService;

    public SearchApiController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/api/search")
    public SearchApiResponse search(@RequestParam(required = false) String q) {
        String query = q == null ? "" : q.trim();
        if (query.isEmpty()) {
            return new SearchApiResponse(List.of(), 0);
        }
        if (query.length() > MAX_QUERY_LENGTH) {
            query = query.substring(0, MAX_QUERY_LENGTH);
        }

        SearchService.SearchResults results = searchService.search(query);
        List<SearchResultDto> items = new ArrayList<>();
        results.projects().forEach(project -> items.add(toProjectResult(project)));
        results.posts().forEach(post -> items.add(toPostResult(post)));
        results.resources().forEach(resource -> items.add(toResourceResult(resource)));
        return new SearchApiResponse(List.copyOf(items), items.size());
    }

    private SearchResultDto toProjectResult(Project project) {
        return new SearchResultDto(
                "project",
                project.getTitle(),
                project.getSlug(),
                project.getSummary(),
                "/projects/" + project.getSlug()
        );
    }

    private SearchResultDto toPostResult(Post post) {
        return new SearchResultDto(
                "post",
                post.getTitle(),
                post.getSlug(),
                post.getSummary(),
                "/notes/" + post.getSlug()
        );
    }

    private SearchResultDto toResourceResult(Resource resource) {
        return new SearchResultDto(
                "resource",
                resource.getTitle(),
                resource.getSlug(),
                resource.getSummary(),
                "/resources/" + resource.getSlug()
        );
    }

    public record SearchResultDto(String type, String title, String slug, String summary, String url) {
    }

    public record SearchApiResponse(List<SearchResultDto> results, long total) {
    }
}
