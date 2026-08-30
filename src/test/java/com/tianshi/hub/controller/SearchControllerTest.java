package com.tianshi.hub.controller;

import com.tianshi.hub.service.SearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.AbstractView;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SearchService searchService;

    @Test
    void search_空查询_重定向首页() throws Exception {
        redirectMockMvc().perform(get("/search").param("q", "   "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void search_正常查询_返回结果页并传递查询词() throws Exception {
        when(searchService.search("spring")).thenReturn(new SearchService.SearchResults(List.of(), List.of(), List.of()));

        resultMockMvc().perform(get("/search").param("q", " spring "))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attribute("q", "spring"));

        verify(searchService).search("spring");
    }

    @Test
    void search_超长查询_截断到50字符() throws Exception {
        String query = "x".repeat(51);
        when(searchService.search(anyString())).thenReturn(new SearchService.SearchResults(List.of(), List.of(), List.of()));

        resultMockMvc().perform(get("/search").param("q", query))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attribute("q", "x".repeat(50)));

        verify(searchService).search("x".repeat(50));
    }

    private MockMvc resultMockMvc() {
        return MockMvcBuilders.standaloneSetup(new SearchController(searchService))
                .setSingleView(new AbstractView() {
                    @Override
                    protected void renderMergedOutputModel(Map<String, Object> model,
                                                           jakarta.servlet.http.HttpServletRequest request,
                                                           jakarta.servlet.http.HttpServletResponse response) {
                    }
                })
                .build();
    }

    private MockMvc redirectMockMvc() {
        return MockMvcBuilders.standaloneSetup(new SearchController(searchService)).build();
    }
}
