UPDATE posts
SET content = 'Controller 负责参数绑定，Service 负责业务编排，Repository 负责数据访问。

```java
@GetMapping("/{slug}")
public String detail(@PathVariable String slug, Model model) {
    Post post = postService.findPublishedPostBySlug(slug);
    model.addAttribute("renderedContent", markdownService.render(post.getContent()));
    return "notes/detail";
}
```

![Markdown 示例图](/uploads/markdown-preview.png)

如果需要播放本地视频，也可以直接写成：

[演示视频](/uploads/demo-intro.webm)'
WHERE slug = 'spring-boot-layering-checklist';
