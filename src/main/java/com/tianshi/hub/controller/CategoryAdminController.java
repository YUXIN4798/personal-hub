package com.tianshi.hub.controller;

import com.tianshi.hub.dto.CategoryForm;
import com.tianshi.hub.entity.Category;
import com.tianshi.hub.service.CategoryAdminService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
public class CategoryAdminController {

    private final CategoryAdminService categoryAdminService;

    public CategoryAdminController(CategoryAdminService categoryAdminService) {
        this.categoryAdminService = categoryAdminService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categoryRows", categoryAdminService.findRows());
        model.addAttribute("typeLabels", categoryAdminService.typeLabels());
        model.addAttribute("pageTitle", "分类管理");
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        addFormAttributes(model, "新建分类", "/admin/categories/new", false);
        return "admin/categories/form";
    }

    @PostMapping("/new")
    public String create(
            @Valid @ModelAttribute("categoryForm") CategoryForm categoryForm,
            BindingResult bindingResult,
            Model model
    ) {
        rejectInvalidType(categoryForm, bindingResult);
        rejectDuplicates(categoryForm, null, bindingResult);
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, "新建分类", "/admin/categories/new", false);
            return "admin/categories/form";
        }
        try {
            categoryAdminService.create(categoryForm);
        } catch (DataIntegrityViolationException exception) {
            rejectUniqueConflict(exception, bindingResult);
            addFormAttributes(model, "新建分类", "/admin/categories/new", false);
            return "admin/categories/form";
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryAdminService.findCategory(id);
        model.addAttribute("categoryForm", categoryAdminService.toForm(category));
        model.addAttribute("typeLabel", categoryAdminService.labelForType(category.getType()));
        addFormAttributes(model, "编辑分类", "/admin/categories/" + id + "/edit", true);
        return "admin/categories/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            @Valid @ModelAttribute("categoryForm") CategoryForm categoryForm,
            BindingResult bindingResult,
            Model model
    ) {
        Category category = categoryAdminService.findCategory(id);
        categoryForm.setType(category.getType());
        rejectDuplicates(categoryForm, id, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("typeLabel", categoryAdminService.labelForType(category.getType()));
            addFormAttributes(model, "编辑分类", "/admin/categories/" + id + "/edit", true);
            return "admin/categories/form";
        }
        try {
            categoryAdminService.update(id, categoryForm);
        } catch (DataIntegrityViolationException exception) {
            rejectUniqueConflict(exception, bindingResult);
            model.addAttribute("typeLabel", categoryAdminService.labelForType(category.getType()));
            addFormAttributes(model, "编辑分类", "/admin/categories/" + id + "/edit", true);
            return "admin/categories/form";
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryAdminService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "分类已删除");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/categories";
    }

    private void rejectDuplicates(CategoryForm form, Long excludedId, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors("name") || bindingResult.hasFieldErrors("type")) {
            return;
        }
        String slug = categoryAdminService.resolveSlug(form.getSlug(), form.getName(), "category");
        if (categoryAdminService.nameExists(form.getName(), form.getType(), excludedId)) {
            bindingResult.rejectValue("name", "duplicate", "同类型下分类名称已存在");
        }
        if (categoryAdminService.slugExists(slug, form.getType(), excludedId)) {
            bindingResult.rejectValue("slug", "duplicate", "同类型下 Slug 已存在");
        }
    }

    private void rejectInvalidType(CategoryForm form, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("type") && !categoryAdminService.supportsType(form.getType())) {
            bindingResult.rejectValue("type", "unsupported", "分类类型不支持");
        }
    }

    private void rejectUniqueConflict(DataIntegrityViolationException exception, BindingResult bindingResult) {
        String message = exception.getMostSpecificCause() == null
                ? exception.getMessage()
                : exception.getMostSpecificCause().getMessage();
        if (message != null && message.contains("name")) {
            bindingResult.rejectValue("name", "duplicate", "同类型下分类名称已存在，请更换");
            return;
        }
        bindingResult.rejectValue("slug", "duplicate", "同类型下 Slug 已存在，请更换");
    }

    private void addFormAttributes(Model model, String title, String action, boolean editing) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("typeLabels", categoryAdminService.typeLabels());
        model.addAttribute("editing", editing);
    }
}
