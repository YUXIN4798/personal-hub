package com.tianshi.hub.controller;

import com.tianshi.hub.dto.CategoryForm;
import com.tianshi.hub.entity.Category;
import com.tianshi.hub.service.CategoryAdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class CategoryAdminControllerTest {

    @Mock
    private CategoryAdminService categoryAdminService;

    @Test
    void list_访问分类管理_返回列表() throws Exception {
        when(categoryAdminService.findRows()).thenReturn(List.of());
        when(categoryAdminService.typeLabels()).thenReturn(Map.of("project", "项目"));

        mockMvc().perform(get("/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories/list"))
                .andExpect(model().attributeExists("categoryRows", "typeLabels"));
    }

    @Test
    void create_有效表单_创建后重定向列表() throws Exception {
        when(categoryAdminService.supportsType("post")).thenReturn(true);
        when(categoryAdminService.resolveSlug("", "学习笔记", "category")).thenReturn("category-auto");
        when(categoryAdminService.nameExists("学习笔记", "post", null)).thenReturn(false);
        when(categoryAdminService.slugExists("category-auto", "post", null)).thenReturn(false);

        mockMvc().perform(post("/admin/categories/new")
                        .param("name", "学习笔记")
                        .param("slug", "")
                        .param("type", "post")
                        .param("sortOrder", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));

        ArgumentCaptor<CategoryForm> captor = ArgumentCaptor.forClass(CategoryForm.class);
        verify(categoryAdminService).create(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo("post");
        assertThat(captor.getValue().getSortOrder()).isEqualTo(4);
    }

    @Test
    void create_name重复_回显表单错误() throws Exception {
        when(categoryAdminService.supportsType("post")).thenReturn(true);
        when(categoryAdminService.resolveSlug("study-notes", "学习笔记", "category")).thenReturn("study-notes");
        when(categoryAdminService.nameExists("学习笔记", "post", null)).thenReturn(true);
        when(categoryAdminService.slugExists("study-notes", "post", null)).thenReturn(false);
        when(categoryAdminService.typeLabels()).thenReturn(Map.of("post", "笔记"));

        mockMvc().perform(post("/admin/categories/new")
                        .param("name", "学习笔记")
                        .param("slug", "study-notes")
                        .param("type", "post"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories/form"))
                .andExpect(model().attributeHasFieldErrors("categoryForm", "name"));

        verify(categoryAdminService, never()).create(any());
    }

    @Test
    void create_唯一键竞态冲突_回显slug错误() throws Exception {
        when(categoryAdminService.supportsType("resource")).thenReturn(true);
        when(categoryAdminService.resolveSlug("dev-docs", "开发文档", "category")).thenReturn("dev-docs");
        when(categoryAdminService.nameExists("开发文档", "resource", null)).thenReturn(false);
        when(categoryAdminService.slugExists("dev-docs", "resource", null)).thenReturn(false);
        when(categoryAdminService.create(any(CategoryForm.class)))
                .thenThrow(new DataIntegrityViolationException("uk_categories_slug_type"));
        when(categoryAdminService.typeLabels()).thenReturn(Map.of("resource", "资源"));

        mockMvc().perform(post("/admin/categories/new")
                        .param("name", "开发文档")
                        .param("slug", "dev-docs")
                        .param("type", "resource"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories/form"))
                .andExpect(model().attributeHasFieldErrors("categoryForm", "slug"));
    }

    @Test
    void edit_提交篡改type_仍按原type更新() throws Exception {
        Category category = new Category();
        category.setId(5L);
        category.setType("project");
        when(categoryAdminService.findCategory(5L)).thenReturn(category);
        when(categoryAdminService.resolveSlug("portfolio", "作品集", "category")).thenReturn("portfolio");
        when(categoryAdminService.nameExists("作品集", "project", 5L)).thenReturn(false);
        when(categoryAdminService.slugExists("portfolio", "project", 5L)).thenReturn(false);

        mockMvc().perform(post("/admin/categories/5/edit")
                        .param("name", "作品集")
                        .param("slug", "portfolio")
                        .param("type", "post"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));

        ArgumentCaptor<CategoryForm> captor = ArgumentCaptor.forClass(CategoryForm.class);
        verify(categoryAdminService).update(eq(5L), captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo("project");
    }

    @Test
    void delete_被引用_带错误提示返回列表() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalStateException("该分类被 2 个内容使用，无法删除"))
                .when(categoryAdminService).delete(7L);

        mockMvc().perform(post("/admin/categories/7/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"))
                .andExpect(flash().attribute("errorMessage", "该分类被 2 个内容使用，无法删除"));
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(new CategoryAdminController(categoryAdminService)).build();
    }
}
