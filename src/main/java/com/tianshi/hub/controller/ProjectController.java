package com.tianshi.hub.controller;

import com.tianshi.hub.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model
    ) {
        model.addAttribute("projects", projectService.findPublishedProjects(page, size));
        model.addAttribute("pageTitle", "作品集");
        return "projects/list";
    }

    @GetMapping("/{slug}")
    public String detail(@PathVariable String slug, Model model) {
        model.addAttribute("project", projectService.findPublishedProjectBySlug(slug));
        model.addAttribute("pageTitle", "作品详情");
        return "projects/detail";
    }
}
