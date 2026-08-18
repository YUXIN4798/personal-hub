package com.tianshi.hub.service;

import com.tianshi.hub.entity.Post;
import com.tianshi.hub.entity.PostTag;
import com.tianshi.hub.entity.Tag;
import com.tianshi.hub.exception.ResourceNotFoundException;
import com.tianshi.hub.repository.CategoryRepository;
import com.tianshi.hub.repository.PostRepository;
import com.tianshi.hub.repository.PostTagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostTagRepository postTagRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @Test
    void findPublishedPosts_pageSize越界_夹紧并按发布时间排序() {
        when(postRepository.findByStatus(eq("published"), any(Pageable.class))).thenReturn(Page.empty());
        PostServiceImpl service = service();
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        service.findPublishedPosts(-2, 99);

        verify(postRepository).findByStatus(eq("published"), captor.capture());
        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(24);
        assertThat(pageable.getSort().getOrderFor("publishedAt")).isEqualTo(Sort.Order.desc("publishedAt"));
        assertThat(pageable.getSort().getOrderFor("id")).isEqualTo(Sort.Order.desc("id"));
    }

    @Test
    void findPublishedPostBySlug_草稿文章_抛出404() {
        Post draft = new Post();
        draft.setStatus("draft");
        when(postRepository.findBySlug("draft")).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> service().findPublishedPostBySlug("draft"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("笔记不存在");
    }

    @Test
    void findPostTags_批量文章Id_按文章分组保留标签顺序() {
        Post first = post(1L);
        Post second = post(2L);
        Tag java = tag(10L, "Java");
        Tag spring = tag(11L, "Spring Boot");
        when(postTagRepository.findByPost_IdInOrderByTag_NameAsc(List.of(1L, 2L)))
                .thenReturn(List.of(new PostTag(first, java), new PostTag(first, spring), new PostTag(second, spring)));

        Map<Long, List<Tag>> tags = service().findPostTags(List.of(1L, 2L));

        assertThat(tags).containsOnlyKeys(1L, 2L);
        assertThat(tags.get(1L)).containsExactly(java, spring);
        assertThat(tags.get(2L)).containsExactly(spring);
    }

    private PostServiceImpl service() {
        return new PostServiceImpl(postRepository, postTagRepository, categoryRepository);
    }

    private Post post(Long id) {
        Post post = new Post();
        post.setId(id);
        return post;
    }

    private Tag tag(Long id, String name) {
        Tag tag = new Tag();
        ReflectionTestUtils.setField(tag, "id", id);
        ReflectionTestUtils.setField(tag, "name", name);
        return tag;
    }
}
