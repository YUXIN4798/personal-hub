package com.tianshi.hub.service;

import com.tianshi.hub.dto.TagForm;
import com.tianshi.hub.entity.Tag;
import com.tianshi.hub.repository.PostTagRepository;
import com.tianshi.hub.repository.ProjectTagRepository;
import com.tianshi.hub.repository.ResourceTagRepository;
import com.tianshi.hub.repository.TagRepository;
import com.tianshi.hub.repository.UsageCount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagAdminServiceTest {

    @Mock
    private TagRepository tagRepository;
    @Mock
    private ProjectTagRepository projectTagRepository;
    @Mock
    private ResourceTagRepository resourceTagRepository;
    @Mock
    private PostTagRepository postTagRepository;

    @Test
    void create_slug留空中文名称_自动生成稳定slug并写入实体() {
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TagForm form = new TagForm();
        form.setName("后端工程");
        form.setSlug("");

        service().create(form);

        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).save(captor.capture());
        Tag saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("后端工程");
        assertThat(saved.getSlug()).startsWith("tag-").hasSize(12);
    }

    @Test
    void create_ascii名称_自动生成短横线slug() {
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TagForm form = new TagForm();
        form.setName("Spring Boot");
        form.setSlug(null);

        Tag created = service().create(form);

        assertThat(created.getSlug()).isEqualTo("spring-boot");
    }

    @Test
    void delete_标签存在引用_拒绝删除并提示先解绑() {
        Tag tag = new Tag();
        tag.setId(3L);
        when(tagRepository.findById(3L)).thenReturn(Optional.of(tag));
        when(projectTagRepository.countByTagIdIn(List.of(3L))).thenReturn(List.of(count(3L, 1L)));
        when(resourceTagRepository.countByTagIdIn(List.of(3L))).thenReturn(List.of());
        when(postTagRepository.countByTagIdIn(List.of(3L))).thenReturn(List.of());

        assertThatThrownBy(() -> service().delete(3L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("该标签已被内容使用，请先解绑后再删除");

        verify(tagRepository, never()).delete(any());
    }

    @Test
    void delete_标签无引用_删除实体() {
        Tag tag = new Tag();
        tag.setId(3L);
        when(tagRepository.findById(3L)).thenReturn(Optional.of(tag));
        when(projectTagRepository.countByTagIdIn(List.of(3L))).thenReturn(List.of());
        when(resourceTagRepository.countByTagIdIn(List.of(3L))).thenReturn(List.of());
        when(postTagRepository.countByTagIdIn(List.of(3L))).thenReturn(List.of());

        service().delete(3L);

        verify(tagRepository).delete(tag);
    }

    @Test
    void findRows_批量聚合标签引用计数() {
        Tag first = tag(1L);
        Tag second = tag(2L);
        when(tagRepository.findAllByOrderByNameAsc()).thenReturn(List.of(first, second));
        when(projectTagRepository.countByTagIdIn(List.of(1L, 2L))).thenReturn(List.of(count(1L, 2L)));
        when(resourceTagRepository.countByTagIdIn(List.of(1L, 2L))).thenReturn(List.of(count(2L, 3L)));
        when(postTagRepository.countByTagIdIn(List.of(1L, 2L))).thenReturn(List.of(count(1L, 5L)));

        List<TagAdminService.TagRow> rows = service().findRows();

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).usage().projects()).isEqualTo(2);
        assertThat(rows.get(0).usage().resources()).isZero();
        assertThat(rows.get(0).usage().posts()).isEqualTo(5);
        assertThat(rows.get(1).usage().projects()).isZero();
        assertThat(rows.get(1).usage().resources()).isEqualTo(3);
        assertThat(rows.get(1).usage().posts()).isZero();
        verify(projectTagRepository).countByTagIdIn(List.of(1L, 2L));
        verify(resourceTagRepository).countByTagIdIn(List.of(1L, 2L));
        verify(postTagRepository).countByTagIdIn(List.of(1L, 2L));
    }

    @Test
    void slugExists_排除自身_走排除id查询() {
        when(tagRepository.existsBySlugAndIdNot("spring-boot", 8L)).thenReturn(true);

        boolean exists = service().slugExists("spring-boot", 8L);

        assertThat(exists).isTrue();
    }

    private TagAdminService service() {
        return new TagAdminService(
                tagRepository,
                projectTagRepository,
                resourceTagRepository,
                postTagRepository,
                new AdminSlugService()
        );
    }

    private Tag tag(Long id) {
        Tag tag = new Tag();
        tag.setId(id);
        return tag;
    }

    private UsageCount count(Long id, long total) {
        return new UsageCount() {
            @Override
            public Long getId() {
                return id;
            }

            @Override
            public long getTotal() {
                return total;
            }
        };
    }
}
