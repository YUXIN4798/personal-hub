package com.tianshi.hub.controller;

import com.tianshi.hub.entity.Project;
import com.tianshi.hub.exception.GlobalExceptionHandler;
import com.tianshi.hub.service.MarkdownService;
import com.tianshi.hub.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @Test
    void detail_markdown内容_返回渲染后的HTML() throws Exception {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", 1L);
        project.setTitle("Project");
        project.setSlug("project");
        project.setDescription("```java\nclass A {}\n```");
        when(projectService.findPublishedProjectBySlug("project")).thenReturn(project);
        when(projectService.findProjectTags(1L)).thenReturn(java.util.List.of());

        mockMvc().perform(get("/projects/project"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/detail"))
                .andExpect(model().attributeExists("renderedDescription"));
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(new ProjectController(projectService, new MarkdownService()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }
}
