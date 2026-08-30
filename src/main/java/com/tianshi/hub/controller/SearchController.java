package com.tianshi.hub.controller;

import com.tianshi.hub.service.SearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {

    private static final int MAX_QUERY_LENGTH = 50;

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q, Model model) {
        String query = q == null ? "" : q.trim();
        if (query.isEmpty()) {
            return "redirect:/";
        }
        if (query.length() > MAX_QUERY_LENGTH) {
            query = query.substring(0, MAX_QUERY_LENGTH);
        }

        SearchService.SearchResults results = searchService.search(query);
        model.addAttribute("projects", results.projects());
        model.addAttribute("posts", results.posts());
        model.addAttribute("resources", results.resources());
        model.addAttribute("q", query);
        model.addAttribute("pageTitle", "搜索");
        return "search";
    }
}
