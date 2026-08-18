package com.tianshi.hub.service;

import com.tianshi.hub.entity.Resource;
import com.tianshi.hub.exception.ResourceNotFoundException;
import com.tianshi.hub.config.AppProperties;
import com.tianshi.hub.repository.CategoryRepository;
import com.tianshi.hub.repository.ResourceRepository;
import com.tianshi.hub.repository.ResourceTagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceImplTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ResourceTagRepository resourceTagRepository;

    @TempDir
    private Path tempDir;

    @Test
    void findPublicResources_pageSize越界_夹紧到安全范围并稳定排序() {
        when(resourceRepository.findByVisibility(eq("public"), any(Pageable.class))).thenReturn(Page.empty());
        ResourceServiceImpl service = new ResourceServiceImpl(resourceRepository, categoryRepository);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        service.findPublicResources(-3, 999, null);

        verify(resourceRepository).findByVisibility(eq("public"), captor.capture());
        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(24);
        assertThat(pageable.getSort().getOrderFor("createdAt")).isEqualTo(Sort.Order.desc("createdAt"));
        assertThat(pageable.getSort().getOrderFor("id")).isEqualTo(Sort.Order.desc("id"));
    }

    @Test
    void findPublicResources_size非法_使用默认分页大小() {
        when(resourceRepository.findByVisibility(eq("public"), any(Pageable.class))).thenReturn(Page.empty());
        ResourceServiceImpl service = new ResourceServiceImpl(resourceRepository, categoryRepository);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        service.findPublicResources(2, 0, null);

        verify(resourceRepository).findByVisibility(eq("public"), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(captor.getValue().getPageSize()).isEqualTo(6);
    }

    @Test
    void findPublicResources_指定分类_按公开可见过滤() {
        Resource publicResource = resource("public");
        when(resourceRepository.findByVisibilityAndCategoryId(
                eq("public"),
                eq(9L),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(publicResource)));
        ResourceServiceImpl service = new ResourceServiceImpl(resourceRepository, categoryRepository);

        Page<Resource> resources = service.findPublicResources(0, 6, 9L);

        assertThat(resources.getContent()).containsExactly(publicResource);
        verify(resourceRepository, never()).findByVisibility(anyString(), any(Pageable.class));
    }

    @Test
    void findPublicResourceById_private资源_抛出不存在() {
        when(resourceRepository.findById(5L)).thenReturn(Optional.of(resource("private")));
        ResourceServiceImpl service = new ResourceServiceImpl(resourceRepository, categoryRepository);

        assertThatThrownBy(() -> service.findPublicResourceById(5L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("资源不存在");
    }

    @Test
    void findPublicResourceBySlug_private资源_抛出不存在() {
        when(resourceRepository.findBySlug("hidden")).thenReturn(Optional.of(resource("private")));
        ResourceServiceImpl service = new ResourceServiceImpl(resourceRepository, categoryRepository);

        assertThatThrownBy(() -> service.findPublicResourceBySlug("hidden"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("资源不存在");
    }

    @Test
    void prepareDownload_文件不存在_抛出404且不增加计数() {
        Resource resource = resource("public");
        ReflectionTestUtils.setField(resource, "id", 7L);
        ReflectionTestUtils.setField(resource, "filePath", tempDir.resolve("missing.pdf").toString());
        when(resourceRepository.findById(7L)).thenReturn(Optional.of(resource));
        ResourceServiceImpl service = new ResourceServiceImpl(resourceRepository, categoryRepository);

        assertThatThrownBy(() -> service.prepareDownload(7L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("资源文件暂不可下载");
        verify(resourceRepository, never()).incrementDownloadCount(7L);
    }

    @Test
    void prepareDownload_上传目录文件_按服务解析并返回路径() throws Exception {
        AppProperties properties = new AppProperties();
        properties.setUploadDir(tempDir.toString());
        properties.getUpload().setMaxSize(DataSize.ofMegabytes(5));
        FileStorageService fileStorageService = new FileStorageService(properties);
        Path file = tempDir.resolve("sample.png");
        java.nio.file.Files.writeString(file, "content");
        Resource resource = resource("public");
        ReflectionTestUtils.setField(resource, "id", 8L);
        ReflectionTestUtils.setField(resource, "filePath", "/uploads/sample.png");
        when(resourceRepository.findById(8L)).thenReturn(Optional.of(resource));

        ResourceServiceImpl service = new ResourceServiceImpl(
                resourceRepository, categoryRepository, resourceTagRepository, fileStorageService
        );

        ResourceService.ResourceDownload download = service.prepareDownload(8L);

        assertThat(download.path()).isEqualTo(file);
        verify(resourceRepository).incrementDownloadCount(8L);
    }

    @Test
    void isDownloadAvailable_文件不存在_false() {
        Resource resource = resource("public");
        ReflectionTestUtils.setField(resource, "filePath", tempDir.resolve("missing.pdf").toString());
        ResourceServiceImpl service = new ResourceServiceImpl(resourceRepository, categoryRepository);

        assertThat(service.isDownloadAvailable(resource)).isFalse();
    }

    @Test
    void isDownloadAvailable_上传目录文件_true() throws Exception {
        AppProperties properties = new AppProperties();
        properties.setUploadDir(tempDir.toString());
        properties.getUpload().setMaxSize(DataSize.ofMegabytes(5));
        FileStorageService fileStorageService = new FileStorageService(properties);
        Path file = tempDir.resolve("sample.png");
        java.nio.file.Files.writeString(file, "content");
        Resource resource = resource("public");
        ReflectionTestUtils.setField(resource, "filePath", "/uploads/sample.png");

        ResourceServiceImpl service = new ResourceServiceImpl(
                resourceRepository, categoryRepository, resourceTagRepository, fileStorageService
        );

        assertThat(service.isDownloadAvailable(resource)).isTrue();
    }

    private Resource resource(String visibility) {
        Resource resource = new Resource();
        ReflectionTestUtils.setField(resource, "visibility", visibility);
        return resource;
    }
}
