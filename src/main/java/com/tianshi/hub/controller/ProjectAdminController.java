package com.tianshi.hub.controller;

import com.tianshi.hub.dto.ProjectForm;
import com.tianshi.hub.entity.Category;
import com.tianshi.hub.entity.Project;
import com.tianshi.hub.service.ProjectAdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/projects")
public class ProjectAdminController {

    private final ProjectAdminService projectAdminService;

    public ProjectAdminController(ProjectAdminService projectAdminService) {
        this.projectAdminService = projectAdminService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Project> projects = projectAdminService.findProjects(page);
        Map<Long, Category> categories = projectAdminService.findProjectCategories().stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
        Map<Long, String> projectTags = projectAdminService.findProjectTagNames(
                projects.getContent().stream().map(Project::getId).toList());
        model.addAttribute("projects", projects);
        model.addAttribute("categories", categories);
        model.addAttribute("projectTags", projectTags);
        model.addAttribute("pageTitle", "项目管理");
        return "admin/projects/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("projectForm", new ProjectForm());
        addFormAttributes(model, "新建项目", "/admin/projects/new");
        return "admin/projects/form";
    }

    @PostMapping("/new")
    public String create(
            @Valid @ModelAttribute("projectForm") ProjectForm projectForm,
            BindingResult bindingResult,
            Model model
    ) {
        rejectDuplicateSlug(projectForm, null, bindingResult);
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, "新建项目", "/admin/projects/new");
            return "admin/projects/form";
        }
        projectAdminService.create(projectForm);
        return "redirect:/admin/projects";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Project project = projectAdminService.findProject(id);
        model.addAttribute("projectForm", projectAdminService.toForm(project));
        addFormAttributes(model, "编辑项目", "/admin/projects/" + id + "/edit");
        return "admin/projects/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            @Valid @ModelAttribute("projectForm") ProjectForm projectForm,
            BindingResult bindingResult,
            Model model
    ) {
        rejectDuplicateSlug(projectForm, id, bindingResult);
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, "编辑项目", "/admin/projects/" + id + "/edit");
            return "admin/projects/form";
        }
        projectAdminService.update(id, projectForm);
        return "redirect:/admin/projects";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        projectAdminService.delete(id);
        return "redirect:/admin/projects";
    }

    private void rejectDuplicateSlug(ProjectForm projectForm, Long excludedId, BindingResult bindingResult) {
        if (projectForm.getSlug() != null && projectAdminService.slugExists(projectForm.getSlug().trim(), excludedId)) {
            bindingResult.rejectValue("slug", "duplicate", "Slug 已存在");
        }
    }

    private void addFormAttributes(Model model, String title, String action) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("categories", projectAdminService.findProjectCategories());
        model.addAttribute("tags", projectAdminService.findAllTags());
    }
}
