package com.tianshi.hub.controller;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FrontendTemplateSafetyTest {

    @Test
    void appCss_jsEnabledReveal_默认无Js时仍可见() throws Exception {
        String css = read("src/main/resources/static/css/app.css");

        assertThat(css).contains(".reveal {\n    opacity: 1;");
        assertThat(css).contains(".js-enabled .reveal");
        assertThat(css).contains(".js-enabled .reveal {\n    opacity: 0;");
    }

    @Test
    void templates_资源与文件链接_使用ContextPath表达式() throws Exception {
        String resourceDetail = read("src/main/resources/templates/resources/detail.html");
        String adminFiles = read("src/main/resources/templates/admin/files/list.html");

        assertThat(resourceDetail).contains("th:href=\"@{${resource.url}}\"");
        assertThat(adminFiles).contains("th:href=\"@{${file.url}}\"");
    }

    @Test
    void uploadFetch_失败时_处理非Json响应并提示用户() throws Exception {
        String adminFiles = read("src/main/resources/templates/admin/files/list.html");

        assertThat(adminFiles).contains("content-type");
        assertThat(adminFiles).contains("catch (error)");
        assertThat(adminFiles).contains("上传失败，请稍后再试");
    }

    @Test
    void themeInit_被公共片段复用且标记Js可用() throws Exception {
        String fragment = read("src/main/resources/templates/fragments/theme-init.html");

        assertThat(fragment).contains("js-enabled");
        assertThat(fragment).contains("storedTheme");
    }

    private String read(String relativePath) throws Exception {
        return Files.readString(Path.of(relativePath));
    }
}
