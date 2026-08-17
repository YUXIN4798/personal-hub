package com.tianshi.hub.controller;

import com.tianshi.hub.entity.Post;
import com.tianshi.hub.exception.GlobalExceptionHandler;
import com.tianshi.hub.exception.ResourceNotFoundException;
import com.tianshi.hub.service.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostService postService;

    @Test
    void list_访问笔记列表_返回分页数据() throws Exception {
        when(postService.findPublishedPosts(0, 6)).thenReturn(new PageImpl<>(List.of(new Post())));
        when(postService.findPostCategories()).thenReturn(List.of());

        mockMvc().perform(get("/notes"))
                .andExpect(status().isOk())
                .andExpect(view().name("notes/list"))
                .andExpect(model().attributeExists("posts", "categoryNames"));
    }

    @Test
    void detail_已发布笔记_返回详情和标签() throws Exception {
        Post post = new Post();
        ReflectionTestUtils.setField(post, "id", 4L);
        post.setTitle("Java Notes");
        post.setSlug("java-notes");
        post.setContent("```java\nclass Demo {}\n```");
        post.setStatus("published");
        when(postService.findPublishedPostBySlug("java-notes")).thenReturn(post);
        when(postService.findPostTags(4L)).thenReturn(List.of());

        mockMvc().perform(get("/notes/java-notes"))
                .andExpect(status().isOk())
                .andExpect(view().name("notes/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(model().attributeExists("tags", "renderedContent"));
    }

    @Test
    void detail_草稿或不存在_返回404() throws Exception {
        when(postService.findPublishedPostBySlug("draft-note"))
                .thenThrow(new ResourceNotFoundException("笔记不存在"));

        mockMvc().perform(get("/notes/draft-note"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(new PostController(postService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }
}
