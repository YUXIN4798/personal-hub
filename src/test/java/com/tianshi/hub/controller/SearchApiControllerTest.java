package com.tianshi.hub.controller;

import com.tianshi.hub.entity.Post;
import com.tianshi.hub.entity.Project;
import com.tianshi.hub.entity.Resource;
import com.tianshi.hub.service.SearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SearchApiControllerTest {

    @Mock
    private SearchService searchService;

    @Test
    void search_正常查询_返回合并后的JSON结果() throws Exception {
        Project project = new Project();
        project.setTitle("Spring Project");
        project.setSlug("spring-project");
        project.setSummary("project summary");

        Post post = new Post();
        post.setTitle("Spring Notes");
        post.setSlug("spring-notes");
        post.setSummary("post summary");

        Resource resource = new Resource();
        resource.setTitle("Spring Resource");
        resource.setSlug("spring-resource");
        resource.setSummary("resource summary");

        when(searchService.search("spring")).thenReturn(new SearchService.SearchResults(
                List.of(project),
                List.of(post),
                List.of(resource)
        ));

        mockMvc().perform(get("/api/search").param("q", " spring "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", hasSize(3)))
                .andExpect(jsonPath("$.results[0].type").value("project"))
                .andExpect(jsonPath("$.results[0].title").value("Spring Project"))
                .andExpect(jsonPath("$.results[0].slug").value("spring-project"))
                .andExpect(jsonPath("$.results[0].summary").value("project summary"))
                .andExpect(jsonPath("$.results[0].url").value("/projects/spring-project"))
                .andExpect(jsonPath("$.results[1].type").value("post"))
                .andExpect(jsonPath("$.results[1].url").value("/notes/spring-notes"))
                .andExpect(jsonPath("$.results[2].type").value("resource"))
                .andExpect(jsonPath("$.results[2].url").value("/resources/spring-resource"))
                .andExpect(jsonPath("$.total").value(3));

        verify(searchService).search("spring");
    }

    @Test
    void search_空查询_返回空结果() throws Exception {
        mockMvc().perform(get("/api/search").param("q", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", hasSize(0)))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void search_超长查询_截断后调用服务() throws Exception {
        String query = "x".repeat(51);
        when(searchService.search(anyString())).thenReturn(new SearchService.SearchResults(List.of(), List.of(), List.of()));

        mockMvc().perform(get("/api/search").param("q", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", hasSize(0)))
                .andExpect(jsonPath("$.total").value(0));

        verify(searchService).search("x".repeat(50));
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(new SearchApiController(searchService))
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }
}
