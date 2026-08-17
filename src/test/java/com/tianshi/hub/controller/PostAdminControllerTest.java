package com.tianshi.hub.controller;

import com.tianshi.hub.dto.PostForm;
import com.tianshi.hub.entity.Post;
import com.tianshi.hub.service.PostAdminService;
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
class PostAdminControllerTest {

    @Mock
    private PostAdminService postAdminService;

    @Test
    void list_访问笔记管理_返回分页列表() throws Exception {
        Post post = new Post();
        ReflectionTestUtils.setField(post, "id", 1L);
        when(postAdminService.findPosts(0)).thenReturn(new PageImpl<>(List.of(post)));
        when(postAdminService.findPostCategories()).thenReturn(List.of());
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(get("/admin/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/posts/list"))
                .andExpect(model().attributeExists("posts", "categories", "postTags"));
    }

    @Test
    void create_有效表单_创建后重定向列表() throws Exception {
        when(postAdminService.slugExists("java-notes", null)).thenReturn(false);
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(post("/admin/posts/new")
                        .param("title", "Java Notes")
                        .param("slug", "java-notes")
                        .param("summary", "摘要")
                        .param("content", "正文")
                        .param("status", "published")
                        .param("categoryId", "2")
                        .param("tagIds", "1", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/posts"));

        ArgumentCaptor<PostForm> captor = ArgumentCaptor.forClass(PostForm.class);
        verify(postAdminService).create(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("正文");
        assertThat(captor.getValue().getCategoryId()).isEqualTo(2L);
        assertThat(captor.getValue().getTagIds()).containsExactly(1L, 2L);
    }

    @Test
    void create_slug重复_回显表单错误() throws Exception {
        when(postAdminService.slugExists("exists", null)).thenReturn(true);
        when(postAdminService.findPostCategories()).thenReturn(List.of());
        when(postAdminService.findAllTags()).thenReturn(List.of());

        mockMvc().perform(post("/admin/posts/new")
                        .param("title", "Exists")
                        .param("slug", "exists")
                        .param("content", "正文")
                        .param("status", "draft"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "slug"));

        verify(postAdminService, never()).create(any());
    }

    @Test
    void edit_有效表单_更新后重定向列表() throws Exception {
        when(postAdminService.slugExists("edited", 9L)).thenReturn(false);

        mockMvc().perform(post("/admin/posts/9/edit")
                        .param("title", "Edited")
                        .param("slug", "edited")
                        .param("content", "正文")
                        .param("status", "draft"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/posts"));

        verify(postAdminService).update(eq(9L), any(PostForm.class));
    }

    @Test
    void delete_提交删除_删除后重定向列表() throws Exception {
        mockMvc().perform(post("/admin/posts/7/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/posts"));

        verify(postAdminService).delete(7L);
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(new PostAdminController(postAdminService)).build();
    }
}
