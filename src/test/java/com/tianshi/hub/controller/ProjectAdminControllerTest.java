package com.tianshi.hub.controller;

import com.tianshi.hub.dto.ProjectForm;
import com.tianshi.hub.entity.Project;
import com.tianshi.hub.service.ProjectAdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
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
class ProjectAdminControllerTest {

    @Mock
    private ProjectAdminService projectAdminService;

    @Test
    void list_访问项目管理_返回分页列表() throws Exception {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", 1L);
        ReflectionTestUtils.setField(project, "title", "Personal Hub");
        when(projectAdminService.findProjects(0)).thenReturn(new PageImpl<>(List.of(project)));
        when(projectAdminService.findProjectCategories()).thenReturn(List.of());
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(get("/admin/projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/projects/list"))
                .andExpect(model().attributeExists("projects", "categories"));
    }

    @Test
    void create_有效表单_创建后重定向列表() throws Exception {
        when(projectAdminService.slugExists("new-project", null)).thenReturn(false);
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(post("/admin/projects/new")
                        .param("title", "New Project")
                        .param("slug", "new-project")
                        .param("summary", "项目摘要")
                        .param("description", "项目描述")
                        .param("techStack", "Java, MySQL")
                        .param("featured", "true")
                        .param("sortOrder", "7")
                        .param("status", "published")
                        .param("tagIds", "1", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/projects"));

        ArgumentCaptor<ProjectForm> captor = ArgumentCaptor.forClass(ProjectForm.class);
        verify(projectAdminService).create(captor.capture());
        assertThat(captor.getValue().getSummary()).isEqualTo("项目摘要");
        assertThat(captor.getValue().getTechStack()).isEqualTo("Java, MySQL");
        assertThat(captor.getValue().isFeatured()).isTrue();
        assertThat(captor.getValue().getSortOrder()).isEqualTo(7);
        assertThat(captor.getValue().getTagIds()).containsExactly(1L, 2L);
    }

    @Test
    void create_slug重复_回显表单错误() throws Exception {
        when(projectAdminService.slugExists("exists", null)).thenReturn(true);
        when(projectAdminService.findProjectCategories()).thenReturn(List.of());
        when(projectAdminService.findAllTags()).thenReturn(List.of());
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(post("/admin/projects/new")
                        .param("title", "Exists")
                        .param("slug", "exists")
                        .param("status", "draft"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/projects/form"))
                .andExpect(model().attributeHasFieldErrors("projectForm", "slug"));

        verify(projectAdminService, never()).create(any());
    }

    @Test
    void create_唯一键竞态冲突_回显slug错误() throws Exception {
        when(projectAdminService.slugExists("race", null)).thenReturn(false);
        when(projectAdminService.create(any(ProjectForm.class))).thenThrow(new DataIntegrityViolationException("uk_projects_slug"));
        when(projectAdminService.findProjectCategories()).thenReturn(List.of());
        when(projectAdminService.findAllTags()).thenReturn(List.of());

        mockMvc().perform(post("/admin/projects/new")
                        .param("title", "Race")
                        .param("slug", "race")
                        .param("status", "draft"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/projects/form"))
                .andExpect(model().attributeHasFieldErrors("projectForm", "slug"));
    }

    @Test
    void edit_有效表单_更新后重定向列表() throws Exception {
        when(projectAdminService.slugExists("edited", 9L)).thenReturn(false);
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(post("/admin/projects/9/edit")
                        .param("title", "Edited")
                        .param("slug", "edited")
                        .param("status", "draft"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/projects"));

        verify(projectAdminService).update(eq(9L), any(ProjectForm.class));
    }

    @Test
    void edit_唯一键竞态冲突_回显slug错误() throws Exception {
        when(projectAdminService.slugExists("race", 9L)).thenReturn(false);
        when(projectAdminService.update(eq(9L), any(ProjectForm.class))).thenThrow(new DataIntegrityViolationException("uk_projects_slug"));
        when(projectAdminService.findProjectCategories()).thenReturn(List.of());
        when(projectAdminService.findAllTags()).thenReturn(List.of());

        mockMvc().perform(post("/admin/projects/9/edit")
                        .param("title", "Race")
                        .param("slug", "race")
                        .param("status", "draft"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/projects/form"))
                .andExpect(model().attributeHasFieldErrors("projectForm", "slug"));
    }

    @Test
    void delete_提交删除_删除后重定向列表() throws Exception {
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(post("/admin/projects/7/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/projects"));

        verify(projectAdminService).delete(7L);
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(new ProjectAdminController(projectAdminService)).build();
    }
}
