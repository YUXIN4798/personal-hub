package com.tianshi.hub.controller;

import com.tianshi.hub.dto.ResourceForm;
import com.tianshi.hub.entity.Category;
import com.tianshi.hub.entity.Resource;
import com.tianshi.hub.service.ResourceAdminService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
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
@RequestMapping("/admin/resources")
public class ResourceAdminController {

    private final ResourceAdminService resourceAdminService;

    public ResourceAdminController(ResourceAdminService resourceAdminService) {
        this.resourceAdminService = resourceAdminService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Resource> resources = resourceAdminService.findResources(page);
        Map<Long, Category> categories = resourceAdminService.findResourceCategories().stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
        Map<Long, String> resourceTags = resourceAdminService.findResourceTagNames(
                resources.getContent().stream().map(Resource::getId).toList());
        model.addAttribute("resources", resources);
        model.addAttribute("categories", categories);
        model.addAttribute("resourceTags", resourceTags);
        model.addAttribute("pageTitle", "资源管理");
        return "admin/resources/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("resourceForm", new ResourceForm());
        addFormAttributes(model, "新建资源", "/admin/resources/new");
        return "admin/resources/form";
    }

    @PostMapping("/new")
    public String create(
            @Valid @ModelAttribute("resourceForm") ResourceForm resourceForm,
            BindingResult bindingResult,
            Model model
    ) {
        rejectDuplicateSlug(resourceForm, null, bindingResult);
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, "新建资源", "/admin/resources/new");
            return "admin/resources/form";
        }
        try {
            resourceAdminService.create(resourceForm);
        } catch (DataIntegrityViolationException exception) {
            bindingResult.rejectValue("slug", "duplicate", "Slug 已存在");
            addFormAttributes(model, "新建资源", "/admin/resources/new");
            return "admin/resources/form";
        }
        return "redirect:/admin/resources";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Resource resource = resourceAdminService.findResource(id);
        model.addAttribute("resourceForm", resourceAdminService.toForm(resource));
        addFormAttributes(model, "编辑资源", "/admin/resources/" + id + "/edit");
        return "admin/resources/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            @Valid @ModelAttribute("resourceForm") ResourceForm resourceForm,
            BindingResult bindingResult,
            Model model
    ) {
        rejectDuplicateSlug(resourceForm, id, bindingResult);
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, "编辑资源", "/admin/resources/" + id + "/edit");
            return "admin/resources/form";
        }
        try {
            resourceAdminService.update(id, resourceForm);
        } catch (DataIntegrityViolationException exception) {
            bindingResult.rejectValue("slug", "duplicate", "Slug 已存在");
            addFormAttributes(model, "编辑资源", "/admin/resources/" + id + "/edit");
            return "admin/resources/form";
        }
        return "redirect:/admin/resources";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        resourceAdminService.delete(id);
        return "redirect:/admin/resources";
    }

    private void rejectDuplicateSlug(ResourceForm resourceForm, Long excludedId, BindingResult bindingResult) {
        if (resourceForm.getSlug() != null && resourceAdminService.slugExists(resourceForm.getSlug().trim(), excludedId)) {
            bindingResult.rejectValue("slug", "duplicate", "Slug 已存在");
        }
    }

    private void addFormAttributes(Model model, String title, String action) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("categories", resourceAdminService.findResourceCategories());
        model.addAttribute("tags", resourceAdminService.findAllTags());
    }
}
