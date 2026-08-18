package com.tianshi.hub.controller;

import com.tianshi.hub.dto.ResourceForm;
import com.tianshi.hub.entity.Resource;
import com.tianshi.hub.service.ResourceAdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class ResourceAdminControllerTest {

    @Mock
    private ResourceAdminService resourceAdminService;

    @Test
    void list_访问资源管理_返回分页列表() throws Exception {
        Resource resource = new Resource();
        resource.setId(1L);
        resource.setTitle("Spring Boot Notes");
        when(resourceAdminService.findResources(0)).thenReturn(new PageImpl<>(List.of(resource)));
        when(resourceAdminService.findResourceCategories()).thenReturn(List.of());
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(get("/admin/resources"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/resources/list"))
                .andExpect(model().attributeExists("resources", "categories", "resourceTags"));
    }

    @Test
    void create_有效表单_创建后重定向列表() throws Exception {
        when(resourceAdminService.slugExists("new-resource", null)).thenReturn(false);
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(post("/admin/resources/new")
                        .param("title", "New Resource")
                        .param("slug", "new-resource")
                        .param("summary", "资源摘要")
                        .param("description", "资源描述")
                        .param("url", "https://example.com/resource")
                        .param("type", "link")
                        .param("categoryId", "3")
                        .param("tagIds", "1", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/resources"));

        ArgumentCaptor<ResourceForm> captor = ArgumentCaptor.forClass(ResourceForm.class);
        verify(resourceAdminService).create(captor.capture());
        assertThat(captor.getValue().getSummary()).isEqualTo("资源摘要");
        assertThat(captor.getValue().getDescription()).isEqualTo("资源描述");
        assertThat(captor.getValue().getUrl()).isEqualTo("https://example.com/resource");
        assertThat(captor.getValue().getType()).isEqualTo("link");
        assertThat(captor.getValue().getCategoryId()).isEqualTo(3L);
        assertThat(captor.getValue().getTagIds()).containsExactly(1L, 2L);
    }

    @Test
    void create_slug重复_回显表单错误() throws Exception {
        when(resourceAdminService.slugExists("exists", null)).thenReturn(true);
        when(resourceAdminService.findResourceCategories()).thenReturn(List.of());
        when(resourceAdminService.findAllTags()).thenReturn(List.of());
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(post("/admin/resources/new")
                        .param("title", "Exists")
                        .param("slug", "exists")
                        .param("url", "https://example.com/exists")
                        .param("type", "link"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/resources/form"))
                .andExpect(model().attributeHasFieldErrors("resourceForm", "slug"));

        verify(resourceAdminService, never()).create(any());
    }

    @Test
    void create_javascript协议URL_回显表单错误且不创建() throws Exception {
        when(resourceAdminService.slugExists("bad-link", null)).thenReturn(false);
        when(resourceAdminService.findResourceCategories()).thenReturn(List.of());
        when(resourceAdminService.findAllTags()).thenReturn(List.of());
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(post("/admin/resources/new")
                        .param("title", "Bad Link")
                        .param("slug", "bad-link")
                        .param("url", "javascript:alert(1)")
                        .param("type", "link"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/resources/form"))
                .andExpect(model().attributeHasFieldErrors("resourceForm", "url"));

        verify(resourceAdminService, never()).create(any());
    }

    @Test
    void create_协议相对URL_回显表单错误且不创建() throws Exception {
        when(resourceAdminService.slugExists("protocol-relative", null)).thenReturn(false);
        when(resourceAdminService.findResourceCategories()).thenReturn(List.of());
        when(resourceAdminService.findAllTags()).thenReturn(List.of());
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(post("/admin/resources/new")
                        .param("title", "Protocol Relative")
                        .param("slug", "protocol-relative")
                        .param("url", "//evil.example/resource")
                        .param("type", "link"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/resources/form"))
                .andExpect(model().attributeHasFieldErrors("resourceForm", "url"));

        verify(resourceAdminService, never()).create(any());
    }

    @Test
    void edit_有效表单_更新后重定向列表() throws Exception {
        when(resourceAdminService.slugExists("edited", 9L)).thenReturn(false);
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(post("/admin/resources/9/edit")
                        .param("title", "Edited")
                        .param("slug", "edited")
                        .param("url", "https://example.com/edited")
                        .param("type", "file"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/resources"));

        verify(resourceAdminService).update(eq(9L), any(ResourceForm.class));
    }

    @Test
    void delete_提交删除_删除后重定向列表() throws Exception {
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(post("/admin/resources/7/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/resources"));

        verify(resourceAdminService).delete(7L);
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(new ResourceAdminController(resourceAdminService)).build();
    }
}
