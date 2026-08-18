package com.tianshi.hub.controller;

import com.tianshi.hub.dto.TagForm;
import com.tianshi.hub.service.TagAdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class TagAdminControllerTest {

    @Mock
    private TagAdminService tagAdminService;

    @Test
    void list_访问标签管理_返回列表() throws Exception {
        when(tagAdminService.findRows()).thenReturn(List.of());

        mockMvc().perform(get("/admin/tags"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tags/list"))
                .andExpect(model().attributeExists("tagRows"));
    }

    @Test
    void create_有效表单_创建后重定向列表() throws Exception {
        when(tagAdminService.resolveSlug("", "后端工程", "tag")).thenReturn("tag-auto");
        when(tagAdminService.nameExists("后端工程", null)).thenReturn(false);
        when(tagAdminService.slugExists("tag-auto", null)).thenReturn(false);

        mockMvc().perform(post("/admin/tags/new")
                        .param("name", "后端工程")
                        .param("slug", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tags"));

        ArgumentCaptor<TagForm> captor = ArgumentCaptor.forClass(TagForm.class);
        verify(tagAdminService).create(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("后端工程");
    }

    @Test
    void create_slug重复_回显表单错误() throws Exception {
        when(tagAdminService.resolveSlug("java", "Java", "tag")).thenReturn("java");
        when(tagAdminService.nameExists("Java", null)).thenReturn(false);
        when(tagAdminService.slugExists("java", null)).thenReturn(true);

        mockMvc().perform(post("/admin/tags/new")
                        .param("name", "Java")
                        .param("slug", "java"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tags/form"))
                .andExpect(model().attributeHasFieldErrors("tagForm", "slug"));

        verify(tagAdminService, never()).create(any());
    }

    @Test
    void create_唯一键竞态冲突_回显slug错误() throws Exception {
        when(tagAdminService.resolveSlug("java", "Java", "tag")).thenReturn("java");
        when(tagAdminService.nameExists("Java", null)).thenReturn(false);
        when(tagAdminService.slugExists("java", null)).thenReturn(false);
        when(tagAdminService.create(any(TagForm.class))).thenThrow(new DataIntegrityViolationException("uk_tags_slug"));

        mockMvc().perform(post("/admin/tags/new")
                        .param("name", "Java")
                        .param("slug", "java"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tags/form"))
                .andExpect(model().attributeHasFieldErrors("tagForm", "slug"));
    }

    @Test
    void edit_有效表单_更新后重定向列表() throws Exception {
        when(tagAdminService.resolveSlug("spring-boot", "Spring Boot", "tag")).thenReturn("spring-boot");
        when(tagAdminService.nameExists("Spring Boot", 9L)).thenReturn(false);
        when(tagAdminService.slugExists("spring-boot", 9L)).thenReturn(false);

        mockMvc().perform(post("/admin/tags/9/edit")
                        .param("name", "Spring Boot")
                        .param("slug", "spring-boot"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tags"));

        verify(tagAdminService).update(eq(9L), any(TagForm.class));
    }

    @Test
    void delete_被引用_带错误提示返回列表() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalStateException("该标签已被内容使用，请先解绑后再删除"))
                .when(tagAdminService).delete(7L);

        mockMvc().perform(post("/admin/tags/7/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tags"))
                .andExpect(flash().attribute("errorMessage", "该标签已被内容使用，请先解绑后再删除"));
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(new TagAdminController(tagAdminService)).build();
    }
}
