package com.tianshi.hub.service;

import com.tianshi.hub.dto.ProjectForm;
import com.tianshi.hub.entity.Project;
import com.tianshi.hub.repository.CategoryRepository;
import com.tianshi.hub.repository.ProjectRepository;
import com.tianshi.hub.repository.ProjectTagRepository;
import com.tianshi.hub.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectAdminServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ProjectTagRepository projectTagRepository;

    @Test
    void create_表单字段_完整写入实体() {
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            ReflectionTestUtils.setField(project, "id", 8L);
            return project;
        });
        when(tagRepository.findAllById(List.of())).thenReturn(List.of());
        ProjectAdminService service = service();
        ProjectForm form = new ProjectForm();
        form.setTitle("Personal Hub");
        form.setSlug("personal-hub");
        form.setSummary("列表摘要");
        form.setDescription("详情描述");
        form.setTechStack("Java, Spring Boot, MySQL");
        form.setFeatured(true);
        form.setSortOrder(3);
        form.setStatus("published");

        service.create(form);

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(captor.capture());
        Project saved = captor.getValue();
        assertThat(saved.getSummary()).isEqualTo("列表摘要");
        assertThat(saved.getDescription()).isEqualTo("详情描述");
        assertThat(saved.getTechStack()).isEqualTo("Java, Spring Boot, MySQL");
        assertThat(saved.isFeatured()).isTrue();
        assertThat(saved.getSortOrder()).isEqualTo(3);
    }

    @Test
    void toForm_实体字段_完整回填表单() {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", 9L);
        project.setTitle("Personal Hub");
        project.setSlug("personal-hub");
        project.setSummary("列表摘要");
        project.setDescription("详情描述");
        project.setTechStack("Java, Spring Boot, MySQL");
        project.setFeatured(true);
        project.setSortOrder(4);
        when(projectTagRepository.findTagsByProjectId(9L)).thenReturn(List.of());
        ProjectAdminService service = service();

        ProjectForm form = service.toForm(project);

        assertThat(form.getSummary()).isEqualTo("列表摘要");
        assertThat(form.getDescription()).isEqualTo("详情描述");
        assertThat(form.getTechStack()).isEqualTo("Java, Spring Boot, MySQL");
        assertThat(form.isFeatured()).isTrue();
        assertThat(form.getSortOrder()).isEqualTo(4);
    }

    private ProjectAdminService service() {
        return new ProjectAdminService(projectRepository, categoryRepository, tagRepository, projectTagRepository);
    }
}
