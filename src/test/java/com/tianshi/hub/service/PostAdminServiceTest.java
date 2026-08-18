package com.tianshi.hub.service;

import com.tianshi.hub.dto.PostForm;
import com.tianshi.hub.entity.Post;
import com.tianshi.hub.repository.CategoryRepository;
import com.tianshi.hub.repository.PostRepository;
import com.tianshi.hub.repository.PostTagRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostAdminServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private PostTagRepository postTagRepository;

    @Test
    void create_表单字段_完整写入实体() {
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            ReflectionTestUtils.setField(post, "id", 8L);
            return post;
        });
        when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of());
        PostForm form = new PostForm();
        form.setTitle("Java Notes");
        form.setSlug("java-notes");
        form.setSummary("摘要");
        form.setContent("正文");
        form.setStatus("published");
        form.setCategoryId(2L);
        form.setTagIds(List.of(1L));

        new PostAdminService(postRepository, categoryRepository, tagRepository, postTagRepository).create(form);

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Java Notes");
        assertThat(captor.getValue().getContent()).isEqualTo("正文");
        assertThat(captor.getValue().getStatus()).isEqualTo("published");
        assertThat(captor.getValue().getCategoryId()).isEqualTo(2L);
    }

    @Test
    void create_重复tagId_保存前去重() {
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            ReflectionTestUtils.setField(post, "id", 8L);
            return post;
        });
        when(tagRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of());
        PostForm form = new PostForm();
        form.setTitle("Java Notes");
        form.setSlug("java-notes");
        form.setContent("正文");
        form.setStatus("published");
        form.setTagIds(List.of(1L, 1L, 2L, 2L));

        new PostAdminService(postRepository, categoryRepository, tagRepository, postTagRepository).create(form);

        verify(tagRepository).findAllById(List.of(1L, 2L));
        verify(postTagRepository, never()).save(any());
    }

    @Test
    void toForm_实体字段_完整回填表单() {
        Post post = new Post();
        ReflectionTestUtils.setField(post, "id", 9L);
        post.setTitle("Java Notes");
        post.setSlug("java-notes");
        post.setSummary("摘要");
        post.setContent("正文");
        post.setStatus("draft");
        post.setCategoryId(3L);
        when(postTagRepository.findTagsByPostId(9L)).thenReturn(List.of());

        PostForm form = new PostAdminService(postRepository, categoryRepository, tagRepository, postTagRepository)
                .toForm(post);

        assertThat(form.getTitle()).isEqualTo("Java Notes");
        assertThat(form.getContent()).isEqualTo("正文");
        assertThat(form.getStatus()).isEqualTo("draft");
        assertThat(form.getCategoryId()).isEqualTo(3L);
    }
}
