package com.tianshi.hub.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownServiceTest {

    private final MarkdownService markdownService = new MarkdownService();

    @Test
    void render_代码块图片和链接_输出安全HTML() {
        String html = markdownService.render("""
                # 标题

                ```java
                System.out.println("hi");
                ```

                ![alt](/uploads/sample.png)

                [video](/uploads/demo.webm)
                """);

        assertThat(html).contains("<pre><code");
        assertThat(html).contains("<img");
        assertThat(html).contains("<video controls preload=\"metadata\"");
        assertThat(html).doesNotContain("<script>");
    }

    @Test
    void render_原始HTML_自动转义() {
        String html = markdownService.render("<script>alert(1)</script>");

        assertThat(html).contains("&lt;script&gt;");
        assertThat(html).doesNotContain("<script>");
    }

    @Test
    void render_javascript视频链接_不输出VideoSrc() {
        String html = markdownService.render("[bad](javascript:alert.webm)");

        assertThat(html).doesNotContain("<video");
        assertThat(html).doesNotContain("javascript:alert.webm");
        assertThat(html).contains("<a href=\"#\">bad</a>");
    }

    @Test
    void render_重复Markdown_返回一致HTML() {
        String markdown = "[video](/uploads/demo.webm)";

        String first = markdownService.render(markdown);
        String second = markdownService.render(markdown);

        assertThat(second).isEqualTo(first);
        assertThat(second).contains("<video controls preload=\"metadata\"");
    }
}
