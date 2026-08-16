package com.tianshi.hub.controller;

import com.tianshi.hub.entity.Resource;
import com.tianshi.hub.service.ResourceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpHeaders;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ResourceControllerTest {

    @Mock
    private ResourceService resourceService;

    @TempDir
    private Path tempDir;

    @Test
    void download_中文原始文件名_响应头包含Utf8Filename() throws Exception {
        Path file = tempDir.resolve("resource.pdf");
        Files.writeString(file, "test resource");
        Resource resource = new Resource();
        ReflectionTestUtils.setField(resource, "originalName", "Java 面试八股文整理.pdf");
        when(resourceService.prepareDownload(7L)).thenReturn(new ResourceService.ResourceDownload(resource, file));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ResourceController(resourceService)).build();

        mockMvc.perform(get("/resources/7/download"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("filename=\"=?UTF-8?Q?")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("filename*=UTF-8''Java%20")));
    }
}
