package com.tianshi.hub.controller;

import com.tianshi.hub.dto.PostForm;
import com.tianshi.hub.entity.Category;
import com.tianshi.hub.entity.Post;
import com.tianshi.hub.service.PostAdminService;
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
@RequestMapping("/admin/posts")
public class PostAdminController {

    private final PostAdminService postAdminService;

    public PostAdminController(PostAdminService postAdminService) {
        this.postAdminService = postAdminService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Post> posts = postAdminService.findPosts(page);
        Map<Long, Category> categories = postAdminService.findPostCategories().stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
        Map<Long, String> postTags = postAdminService.findPostTagNames(
                posts.getContent().stream().map(Post::getId).toList());
        model.addAttribute("posts", posts);
        model.addAttribute("categories", categories);
        model.addAttribute("postTags", postTags);
        model.addAttribute("pageTitle", "笔记管理");
        return "admin/posts/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        addFormAttributes(model, "新建笔记", "/admin/posts/new");
        return "admin/posts/form";
    }

    @PostMapping("/new")
    public String create(
            @Valid @ModelAttribute("postForm") PostForm postForm,
            BindingResult bindingResult,
            Model model
    ) {
        rejectDuplicateSlug(postForm, null, bindingResult);
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, "新建笔记", "/admin/posts/new");
            return "admin/posts/form";
        }
        try {
            postAdminService.create(postForm);
        } catch (DataIntegrityViolationException exception) {
            bindingResult.rejectValue("slug", "duplicate", "Slug 已存在，请更换");
            addFormAttributes(model, "新建笔记", "/admin/posts/new");
            return "admin/posts/form";
        }
        return "redirect:/admin/posts";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Post post = postAdminService.findPost(id);
        model.addAttribute("postForm", postAdminService.toForm(post));
        addFormAttributes(model, "编辑笔记", "/admin/posts/" + id + "/edit");
        return "admin/posts/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            @Valid @ModelAttribute("postForm") PostForm postForm,
            BindingResult bindingResult,
            Model model
    ) {
        rejectDuplicateSlug(postForm, id, bindingResult);
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, "编辑笔记", "/admin/posts/" + id + "/edit");
            return "admin/posts/form";
        }
        try {
            postAdminService.update(id, postForm);
        } catch (DataIntegrityViolationException exception) {
            bindingResult.rejectValue("slug", "duplicate", "Slug 已存在，请更换");
            addFormAttributes(model, "编辑笔记", "/admin/posts/" + id + "/edit");
            return "admin/posts/form";
        }
        return "redirect:/admin/posts";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        postAdminService.delete(id);
        return "redirect:/admin/posts";
    }

    private void rejectDuplicateSlug(PostForm postForm, Long excludedId, BindingResult bindingResult) {
        if (postForm.getSlug() != null && postAdminService.slugExists(postForm.getSlug().trim(), excludedId)) {
            bindingResult.rejectValue("slug", "duplicate", "Slug 已存在");
        }
    }

    private void addFormAttributes(Model model, String title, String action) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("categories", postAdminService.findPostCategories());
        model.addAttribute("tags", postAdminService.findAllTags());
    }
}
