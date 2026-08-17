package com.tianshi.hub.service;

import com.tianshi.hub.dto.ResourceForm;
import com.tianshi.hub.entity.Resource;
import com.tianshi.hub.repository.CategoryRepository;
import com.tianshi.hub.repository.ResourceRepository;
import com.tianshi.hub.repository.ResourceTagRepository;
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
class ResourceAdminServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ResourceTagRepository resourceTagRepository;

    @Test
    void create_表单字段_完整写入实体并默认公开() {
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> {
            Resource resource = invocation.getArgument(0);
            ReflectionTestUtils.setField(resource, "id", 8L);
            return resource;
        });
        when(tagRepository.findAllById(List.of())).thenReturn(List.of());
        ResourceAdminService service = service();
        ResourceForm form = new ResourceForm();
        form.setTitle("Spring Boot Notes");
        form.setSlug("spring-boot-notes");
        form.setSummary("资源摘要");
        form.setUrl("https://example.com/notes");
        form.setType("link");
        form.setCategoryId(3L);

        service.create(form);

        ArgumentCaptor<Resource> captor = ArgumentCaptor.forClass(Resource.class);
        verify(resourceRepository).save(captor.capture());
        Resource saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Spring Boot Notes");
        assertThat(saved.getSlug()).isEqualTo("spring-boot-notes");
        assertThat(saved.getSummary()).isEqualTo("资源摘要");
        assertThat(saved.getUrl()).isEqualTo("https://example.com/notes");
        assertThat(saved.getType()).isEqualTo("link");
        assertThat(saved.getCategoryId()).isEqualTo(3L);
        assertThat(saved.getVisibility()).isEqualTo("public");
        assertThat(saved.getVersion()).isEqualTo("v1.0");
    }

    @Test
    void create_file类型_同步文件路径和原始名称() {
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> {
            Resource resource = invocation.getArgument(0);
            ReflectionTestUtils.setField(resource, "id", 8L);
            return resource;
        });
        when(tagRepository.findAllById(List.of())).thenReturn(List.of());
        ResourceAdminService service = service();
        ResourceForm form = new ResourceForm();
        form.setTitle("Courseware.zip");
        form.setSlug("courseware");
        form.setUrl("/uploads/courseware.zip");
        form.setType("file");

        service.create(form);

        ArgumentCaptor<Resource> captor = ArgumentCaptor.forClass(Resource.class);
        verify(resourceRepository).save(captor.capture());
        Resource saved = captor.getValue();
        assertThat(saved.getFilePath()).isEqualTo("/uploads/courseware.zip");
        assertThat(saved.getOriginalName()).isEqualTo("Courseware.zip");
    }

    @Test
    void toForm_实体字段_完整回填表单() {
        Resource resource = new Resource();
        resource.setId(9L);
        resource.setTitle("Spring Boot Notes");
        resource.setSlug("spring-boot-notes");
        resource.setSummary("资源摘要");
        resource.setUrl("https://example.com/notes");
        resource.setType("link");
        resource.setCategoryId(3L);
        when(resourceTagRepository.findTagsByResourceId(9L)).thenReturn(List.of());
        ResourceAdminService service = service();

        ResourceForm form = service.toForm(resource);

        assertThat(form.getTitle()).isEqualTo("Spring Boot Notes");
        assertThat(form.getSlug()).isEqualTo("spring-boot-notes");
        assertThat(form.getSummary()).isEqualTo("资源摘要");
        assertThat(form.getUrl()).isEqualTo("https://example.com/notes");
        assertThat(form.getType()).isEqualTo("link");
        assertThat(form.getCategoryId()).isEqualTo(3L);
    }

    private ResourceAdminService service() {
        return new ResourceAdminService(resourceRepository, categoryRepository, tagRepository, resourceTagRepository);
    }
}

