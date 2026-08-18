package com.tianshi.hub.controller;

import com.tianshi.hub.entity.Category;
import com.tianshi.hub.entity.Resource;
import com.tianshi.hub.service.ResourceService;
import com.tianshi.hub.service.MarkdownService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final MarkdownService markdownService;

    @Autowired
    public ResourceController(ResourceService resourceService, MarkdownService markdownService) {
        this.resourceService = resourceService;
        this.markdownService = markdownService;
    }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) Long category,
            Model model
    ) {
        List<Category> categories = resourceService.findResourceCategories();
        var resources = resourceService.findPublicResources(page, size, category);
        model.addAttribute("resources", resources);
        model.addAttribute("resourceTags", resourceService.findResourceTags(
                resources.getContent().stream().map(resource -> resource.getId()).toList()));
        model.addAttribute("categories", categories);
        model.addAttribute("categoryNames", categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName)));
        model.addAttribute("selectedCategory", category);
        model.addAttribute("pageTitle", "资源库");
        return "resources/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Resource resource = resourceService.findPublicResourceById(id);
        model.addAttribute("resource", resource);
        model.addAttribute("renderedDescription", markdownService.render(resource.getDescription()));
        model.addAttribute("downloadAvailable", resourceService.isDownloadAvailable(resource));
        if (resource.getCategoryId() != null) {
            model.addAttribute("category", resourceService.findCategoryById(resource.getCategoryId()));
        }
        model.addAttribute("tags", resourceService.findResourceTags(resource.getId()));
        model.addAttribute("pageTitle", "资源详情");
        return "resources/detail";
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id) throws IOException {
        ResourceService.ResourceDownload download = resourceService.prepareDownload(id);
        Resource resource = download.resource();
        String fileName = resource.getOriginalName() != null ? resource.getOriginalName() : resource.getTitle();
        InputStreamResource body = new InputStreamResource(Files.newInputStream(download.path()));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(Files.size(download.path()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(body);
    }
}
