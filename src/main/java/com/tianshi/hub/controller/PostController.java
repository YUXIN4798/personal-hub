package com.tianshi.hub.controller;

import com.tianshi.hub.entity.Category;
import com.tianshi.hub.entity.Post;
import com.tianshi.hub.service.PostService;
import com.tianshi.hub.service.MarkdownService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/notes")
public class PostController {

    private final PostService postService;
    private final MarkdownService markdownService;

    public PostController(PostService postService) {
        this(postService, new MarkdownService());
    }

    @Autowired
    public PostController(PostService postService, MarkdownService markdownService) {
        this.postService = postService;
        this.markdownService = markdownService;
    }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model
    ) {
        List<Category> categories = postService.findPostCategories();
        var posts = postService.findPublishedPosts(page, size);
        model.addAttribute("posts", posts);
        model.addAttribute("postTags", postService.findPostTags(
                posts.getContent().stream().map(post -> post.getId()).toList()));
        model.addAttribute("categoryNames", categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (left, right) -> left)));
        model.addAttribute("pageTitle", "笔记");
        return "notes/list";
    }

    @GetMapping("/{slug}")
    public String detail(@PathVariable String slug, Model model) {
        Post post = postService.findPublishedPostBySlug(slug);
        model.addAttribute("post", post);
        model.addAttribute("renderedContent", markdownService.render(post.getContent()));
        model.addAttribute("tags", postService.findPostTags(post.getId()));
        if (post.getCategoryId() != null) {
            model.addAttribute("category", postService.findPostCategory(post.getCategoryId()));
        }
        model.addAttribute("pageTitle", "笔记详情");
        return "notes/detail";
    }
}
