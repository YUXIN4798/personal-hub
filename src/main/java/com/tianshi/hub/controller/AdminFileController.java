package com.tianshi.hub.controller;

import com.tianshi.hub.exception.FileStorageException;
import com.tianshi.hub.service.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminFileController {

    private final FileStorageService fileStorageService;

    public AdminFileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/files")
    public String files(Model model) {
        model.addAttribute("pageTitle", "文件管理");
        model.addAttribute("files", fileStorageService.listImageFiles());
        return "admin/files/list";
    }

    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileStorageService.store(file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (FileStorageException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
        }
    }

    @PostMapping("/files/{name}/delete")
    public String delete(@PathVariable String name) {
        fileStorageService.delete(name);
        return "redirect:/admin/files";
    }
}
