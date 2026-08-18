package com.tianshi.hub.controller;

import com.tianshi.hub.config.AppProperties;
import com.tianshi.hub.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.unit.DataSize;

import java.nio.file.Path;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminFileControllerTest {

    @TempDir
    Path tempDir;

    @Test
    void upload_图片文件_返回上传URL() throws Exception {
        MockMvc mockMvc = mockMvc();
        MockMultipartFile file = new MockMultipartFile("file", "cover.png", "image/png",
                new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x00});

        mockMvc.perform(multipart("/admin/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url", startsWith("/uploads/")));
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(new AdminFileController(fileStorageService())).build();
    }

    private FileStorageService fileStorageService() {
        AppProperties properties = new AppProperties();
        properties.setUploadDir(tempDir.toString());
        properties.getUpload().setMaxSize(DataSize.ofMegabytes(5));
        return new FileStorageService(properties);
    }
}
