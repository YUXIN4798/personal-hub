package com.tianshi.hub.controller;

import com.tianshi.hub.config.AdminSession;
import com.tianshi.hub.service.SiteConfigKeys;
import com.tianshi.hub.service.SiteConfigService;
import com.tianshi.hub.service.CategoryAdminService;
import com.tianshi.hub.service.PostService;
import com.tianshi.hub.service.ResourceService;
import com.tianshi.hub.service.TagAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TemplateRenderingSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResourceService resourceService;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private CategoryAdminService categoryAdminService;

    @MockitoBean
    private TagAdminService tagAdminService;

    @MockitoBean
    private SiteConfigService siteConfigService;

    @Test
    void homePage_完整Web上下文_渲染首页模板() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<!DOCTYPE html>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("正在发生的事")));
    }

    @Test
    void aboutPage_完整Web上下文_渲染Thymeleaf模板() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<!DOCTYPE html>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("关于我")));
    }

    @Test
    void resourcesList_完整Web上下文_渲染Thymeleaf模板() throws Exception {
        when(resourceService.findResourceCategories()).thenReturn(List.of());
        when(resourceService.findPublicResources(anyInt(), anyInt(), any())).thenReturn(new PageImpl<>(List.of()));
        when(resourceService.findResourceTags(List.of())).thenReturn(java.util.Collections.emptyMap());

        mockMvc.perform(get("/resources"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<!DOCTYPE html>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("资源库")));
    }

    @Test
    void notesList_完整Web上下文_渲染Thymeleaf模板() throws Exception {
        when(postService.findPostCategories()).thenReturn(List.of());
        when(postService.findPublishedPosts(anyInt(), anyInt())).thenReturn(new PageImpl<>(List.of()));
        when(postService.findPostTags(List.of())).thenReturn(java.util.Collections.emptyMap());

        mockMvc.perform(get("/notes"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<!DOCTYPE html>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("笔记")));
    }

    @Test
    void adminCategories_完整Web上下文_渲染Thymeleaf模板() throws Exception {
        when(categoryAdminService.findRows()).thenReturn(List.of());
        when(categoryAdminService.typeLabels()).thenReturn(Map.of("project", "项目"));

        mockMvc.perform(get("/admin/categories")
                        .sessionAttr(AdminSession.ADMIN_AUTHENTICATED, true))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<!DOCTYPE html>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("分类管理")));
    }

    @Test
    void adminTags_完整Web上下文_渲染Thymeleaf模板() throws Exception {
        when(tagAdminService.findRows()).thenReturn(List.of());

        mockMvc.perform(get("/admin/tags")
                        .sessionAttr(AdminSession.ADMIN_AUTHENTICATED, true))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<!DOCTYPE html>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("标签管理")));
    }

    @Test
    void adminSiteConfig_完整Web上下文_渲染Thymeleaf模板() throws Exception {
        when(siteConfigService.get(SiteConfigKeys.HERO_TITLE, SiteConfigKeys.DEFAULT_HERO_TITLE)).thenReturn("标题");
        when(siteConfigService.get(SiteConfigKeys.HERO_SUBTITLE, SiteConfigKeys.DEFAULT_HERO_SUBTITLE)).thenReturn("副标题");
        when(siteConfigService.getInt(SiteConfigKeys.PROJECTS_COUNT, SiteConfigKeys.DEFAULT_SECTION_COUNT)).thenReturn(3);
        when(siteConfigService.getInt(SiteConfigKeys.NOTES_COUNT, SiteConfigKeys.DEFAULT_SECTION_COUNT)).thenReturn(2);
        when(siteConfigService.getInt(SiteConfigKeys.RESOURCES_COUNT, SiteConfigKeys.DEFAULT_SECTION_COUNT)).thenReturn(1);

        mockMvc.perform(get("/admin/site-config")
                        .sessionAttr(AdminSession.ADMIN_AUTHENTICATED, true))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<!DOCTYPE html>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("首页设置")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hero 标题")));
    }
}
