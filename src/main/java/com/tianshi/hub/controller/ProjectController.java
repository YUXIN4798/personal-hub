package com.tianshi.hub.controller;

import com.tianshi.hub.service.ProjectService;
import com.tianshi.hub.service.MarkdownService;
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
    private final MarkdownService markdownService;

    public ProjectController(ProjectService projectService, MarkdownService markdownService) {
        this.projectService = projectService;
        this.markdownService = markdownService;
    }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model
    ) {
        var projects = projectService.findPublishedProjects(page, size);
        model.addAttribute("projects", projects);
        model.addAttribute("projectTags", projectService.findProjectTags(
                projects.getContent().stream().map(project -> project.getId()).toList()));
        model.addAttribute("pageTitle", "作品集");
        return "projects/list";
    }

    @GetMapping("/{slug}")
    public String detail(@PathVariable String slug, Model model) {
        var project = projectService.findPublishedProjectBySlug(slug);
        model.addAttribute("project", project);
        model.addAttribute("renderedDescription", markdownService.render(project.getDescription()));
        model.addAttribute("tags", projectService.findProjectTags(project.getId()));
        model.addAttribute("pageTitle", "作品详情");
        return "projects/detail";
    }
}
