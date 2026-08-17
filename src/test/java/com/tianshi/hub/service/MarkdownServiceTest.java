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
}
