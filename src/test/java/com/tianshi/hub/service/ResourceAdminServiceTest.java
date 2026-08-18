package com.tianshi.hub.service;

import com.tianshi.hub.dto.ResourceForm;
import com.tianshi.hub.entity.Resource;
import com.tianshi.hub.repository.CategoryRepository;
import com.tianshi.hub.repository.ResourceRepository;
import com.tianshi.hub.repository.ResourceTagRepository;
import com.tianshi.hub.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @TempDir
    Path tempDir;

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
        form.setDescription("资源描述");
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
        assertThat(saved.getDescription()).isEqualTo("资源描述");
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
        resource.setDescription("资源描述");
        resource.setUrl("https://example.com/notes");
        resource.setType("link");
        resource.setCategoryId(3L);
        when(resourceTagRepository.findTagsByResourceId(9L)).thenReturn(List.of());
        ResourceAdminService service = service();

        ResourceForm form = service.toForm(resource);

        assertThat(form.getTitle()).isEqualTo("Spring Boot Notes");
        assertThat(form.getSlug()).isEqualTo("spring-boot-notes");
        assertThat(form.getSummary()).isEqualTo("资源摘要");
        assertThat(form.getDescription()).isEqualTo("资源描述");
        assertThat(form.getUrl()).isEqualTo("https://example.com/notes");
        assertThat(form.getType()).isEqualTo("link");
        assertThat(form.getCategoryId()).isEqualTo(3L);
    }

    @Test
    void create_重复tagId_保存前去重() {
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> {
            Resource resource = invocation.getArgument(0);
            resource.setId(8L);
            return resource;
        });
        when(tagRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of());
        ResourceAdminService service = service();
        ResourceForm form = new ResourceForm();
        form.setTitle("Spring Boot Notes");
        form.setSlug("spring-boot-notes");
        form.setUrl("https://example.com/notes");
        form.setType("link");
        form.setTagIds(List.of(1L, 1L, 2L, 2L));

        service.create(form);

        verify(tagRepository).findAllById(List.of(1L, 2L));
        verify(resourceTagRepository, never()).save(any());
    }

    @Test
    void create_上传文件_写入文件路径和原始文件名() {
        FileStorageService fileStorageService = fileStorageService();
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> {
            Resource resource = invocation.getArgument(0);
            ReflectionTestUtils.setField(resource, "id", 12L);
            return resource;
        });
        when(tagRepository.findAllById(List.of())).thenReturn(List.of());
        ResourceAdminService service = new ResourceAdminService(
                resourceRepository, categoryRepository, tagRepository, resourceTagRepository, fileStorageService
        );
        ResourceForm form = new ResourceForm();
        form.setTitle("Courseware");
        form.setSlug("courseware");
        form.setType("link");
        form.setFile(new MockMultipartFile("file", "notes.pdf", "application/pdf", new byte[] {1, 2, 3}));

        service.create(form);

        ArgumentCaptor<Resource> captor = ArgumentCaptor.forClass(Resource.class);
        verify(resourceRepository).save(captor.capture());
        Resource saved = captor.getValue();
        assertThat(saved.getFilePath()).startsWith("/uploads/");
        assertThat(saved.getUrl()).startsWith("/uploads/");
        assertThat(saved.getOriginalName()).isEqualTo("notes.pdf");
        assertThat(saved.getFileSize()).isEqualTo(3);
        assertThat(saved.getChecksum()).isEqualTo("039058c6f2c0cb492c533b0a4d14ef77cc0f78abccced5287d84a1a2011cfb81");
    }

    @Test
    void create_上传文件后数据库保存失败_清理已落盘文件() throws Exception {
        FileStorageService fileStorageService = fileStorageService();
        when(resourceRepository.save(any(Resource.class))).thenThrow(new IllegalStateException("db down"));
        ResourceAdminService service = new ResourceAdminService(
                resourceRepository, categoryRepository, tagRepository, resourceTagRepository, fileStorageService
        );
        ResourceForm form = new ResourceForm();
        form.setTitle("Courseware");
        form.setSlug("courseware");
        form.setType("link");
        form.setFile(new MockMultipartFile("file", "notes.pdf", "application/pdf", new byte[] {1, 2, 3}));

        assertThatThrownBy(() -> service.create(form))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("db down");

        try (java.util.stream.Stream<Path> files = java.nio.file.Files.list(tempDir)) {
            assertThat(files).isEmpty();
        }
    }

    @Test
    void update_替换上传文件_保存后清理旧文件并刷新Checksum() throws Exception {
        FileStorageService fileStorageService = fileStorageService();
        Path oldFile = tempDir.resolve("old.pdf");
        java.nio.file.Files.write(oldFile, new byte[] {9, 8, 7});
        Resource resource = new Resource();
        ReflectionTestUtils.setField(resource, "id", 15L);
        resource.setTitle("Old");
        resource.setSlug("old");
        resource.setUrl("/uploads/old.pdf");
        resource.setType("file");
        resource.setFilePath("/uploads/old.pdf");
        resource.setOriginalName("old.pdf");
        resource.setFileSize(3);
        when(resourceRepository.findById(15L)).thenReturn(java.util.Optional.of(resource));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tagRepository.findAllById(List.of())).thenReturn(List.of());
        ResourceAdminService service = new ResourceAdminService(
                resourceRepository, categoryRepository, tagRepository, resourceTagRepository, fileStorageService
        );
        ResourceForm form = new ResourceForm();
        form.setTitle("New");
        form.setSlug("new");
        form.setType("link");
        form.setFile(new MockMultipartFile("file", "new.pdf", "application/pdf", new byte[] {1, 2, 3}));

        Resource updated = service.update(15L, form);

        assertThat(java.nio.file.Files.exists(oldFile)).isFalse();
        assertThat(updated.getFilePath()).startsWith("/uploads/");
        assertThat(updated.getFilePath()).isNotEqualTo("/uploads/old.pdf");
        assertThat(updated.getChecksum()).isEqualTo("039058c6f2c0cb492c533b0a4d14ef77cc0f78abccced5287d84a1a2011cfb81");
    }

    @Test
    void delete_资源存在关联文件_删除数据库后清理文件() throws Exception {
        FileStorageService fileStorageService = fileStorageService();
        Path file = tempDir.resolve("remove.pdf");
        java.nio.file.Files.write(file, new byte[] {1});
        Resource resource = new Resource();
        ReflectionTestUtils.setField(resource, "id", 16L);
        resource.setFilePath("/uploads/remove.pdf");
        when(resourceRepository.findById(16L)).thenReturn(java.util.Optional.of(resource));
        ResourceAdminService service = new ResourceAdminService(
                resourceRepository, categoryRepository, tagRepository, resourceTagRepository, fileStorageService
        );

        service.delete(16L);

        verify(resourceRepository).delete(resource);
        assertThat(java.nio.file.Files.exists(file)).isFalse();
    }

    private ResourceAdminService service() {
        return new ResourceAdminService(resourceRepository, categoryRepository, tagRepository, resourceTagRepository);
    }

    private FileStorageService fileStorageService() {
        com.tianshi.hub.config.AppProperties properties = new com.tianshi.hub.config.AppProperties();
        properties.setUploadDir(tempDir.toString());
        properties.getUpload().setMaxSize(DataSize.ofMegabytes(5));
        return new FileStorageService(properties);
    }
}
