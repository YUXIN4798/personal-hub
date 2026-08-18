package com.tianshi.hub.controller;

import com.tianshi.hub.dto.TagForm;
import com.tianshi.hub.entity.Tag;
import com.tianshi.hub.service.TagAdminService;
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
@RequestMapping("/admin/tags")
public class TagAdminController {

    private final TagAdminService tagAdminService;

    public TagAdminController(TagAdminService tagAdminService) {
        this.tagAdminService = tagAdminService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("tagRows", tagAdminService.findRows());
        model.addAttribute("pageTitle", "标签管理");
        return "admin/tags/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("tagForm", new TagForm());
        addFormAttributes(model, "新建标签", "/admin/tags/new");
        return "admin/tags/form";
    }

    @PostMapping("/new")
    public String create(
            @Valid @ModelAttribute("tagForm") TagForm tagForm,
            BindingResult bindingResult,
            Model model
    ) {
        rejectDuplicates(tagForm, null, bindingResult);
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, "新建标签", "/admin/tags/new");
            return "admin/tags/form";
        }
        try {
            tagAdminService.create(tagForm);
        } catch (DataIntegrityViolationException exception) {
            rejectUniqueConflict(exception, bindingResult);
            addFormAttributes(model, "新建标签", "/admin/tags/new");
            return "admin/tags/form";
        }
        return "redirect:/admin/tags";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Tag tag = tagAdminService.findTag(id);
        model.addAttribute("tagForm", tagAdminService.toForm(tag));
        addFormAttributes(model, "编辑标签", "/admin/tags/" + id + "/edit");
        return "admin/tags/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            @Valid @ModelAttribute("tagForm") TagForm tagForm,
            BindingResult bindingResult,
            Model model
    ) {
        rejectDuplicates(tagForm, id, bindingResult);
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, "编辑标签", "/admin/tags/" + id + "/edit");
            return "admin/tags/form";
        }
        try {
            tagAdminService.update(id, tagForm);
        } catch (DataIntegrityViolationException exception) {
            rejectUniqueConflict(exception, bindingResult);
            addFormAttributes(model, "编辑标签", "/admin/tags/" + id + "/edit");
            return "admin/tags/form";
        }
        return "redirect:/admin/tags";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tagAdminService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "标签已删除");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/tags";
    }

    private void rejectDuplicates(TagForm form, Long excludedId, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors("name")) {
            return;
        }
        String slug = tagAdminService.resolveSlug(form.getSlug(), form.getName(), "tag");
        if (tagAdminService.nameExists(form.getName(), excludedId)) {
            bindingResult.rejectValue("name", "duplicate", "标签名称已存在");
        }
        if (tagAdminService.slugExists(slug, excludedId)) {
            bindingResult.rejectValue("slug", "duplicate", "Slug 已存在");
        }
    }

    private void rejectUniqueConflict(DataIntegrityViolationException exception, BindingResult bindingResult) {
        String message = exception.getMostSpecificCause() == null
                ? exception.getMessage()
                : exception.getMostSpecificCause().getMessage();
        if (message != null && message.contains("name")) {
            bindingResult.rejectValue("name", "duplicate", "标签名称已存在，请更换");
            return;
        }
        bindingResult.rejectValue("slug", "duplicate", "Slug 已存在，请更换");
    }

    private void addFormAttributes(Model model, String title, String action) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("formAction", action);
    }
}
