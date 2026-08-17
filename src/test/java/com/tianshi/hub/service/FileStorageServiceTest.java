package com.tianshi.hub.service;

import com.tianshi.hub.config.AppProperties;
import com.tianshi.hub.exception.FileStorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void store_图片文件_返回uploads路径并落盘() throws Exception {
        FileStorageService service = service();
        MockMultipartFile file = new MockMultipartFile("file", "cover.png", "image/png", new byte[] {1, 2, 3});

        String path = service.store(file);

        assertThat(path).startsWith("/uploads/");
        assertThat(Files.exists(service.resolve(path))).isTrue();
    }

    @Test
    void store_非法扩展名_拒绝保存() {
        FileStorageService service = service();
        MockMultipartFile file = new MockMultipartFile("file", "cover.exe", "application/octet-stream", new byte[] {1});

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(FileStorageException.class)
                .hasMessage("不支持的文件类型");
    }

    @Test
    void resolve_目录穿越_拒绝解析() {
        FileStorageService service = service();

        assertThatThrownBy(() -> service.resolve("../secret.png"))
                .isInstanceOf(FileStorageException.class)
                .hasMessage("非法文件路径");
    }

    private FileStorageService service() {
        AppProperties properties = new AppProperties();
        properties.setUploadDir(tempDir.toString());
        properties.getUpload().setMaxSize(DataSize.ofMegabytes(5));
        return new FileStorageService(properties);
    }
}
