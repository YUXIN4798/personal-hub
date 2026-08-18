package com.tianshi.hub.service;

import com.tianshi.hub.dto.CategoryForm;
import com.tianshi.hub.entity.Category;
import com.tianshi.hub.repository.CategoryRepository;
import com.tianshi.hub.repository.PostRepository;
import com.tianshi.hub.repository.ProjectRepository;
import com.tianshi.hub.repository.ResourceRepository;
import com.tianshi.hub.repository.UsageCount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryAdminServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ResourceRepository resourceRepository;
    @Mock
    private PostRepository postRepository;

    @Test
    void create_slug留空中文名称_自动生成稳定slug并写入实体() {
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CategoryForm form = new CategoryForm();
        form.setName("学习笔记");
        form.setSlug("");
        form.setType("post");
        form.setSortOrder(2);

        service().create(form);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        Category saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("学习笔记");
        assertThat(saved.getSlug()).startsWith("category-").hasSize(17);
        assertThat(saved.getType()).isEqualTo("post");
        assertThat(saved.getSortOrder()).isEqualTo(2);
    }

    @Test
    void update_提交篡改type_保持原分类type不变() {
        Category category = new Category();
        category.setId(7L);
        category.setName("作品集");
        category.setSlug("portfolio");
        category.setType("project");
        when(categoryRepository.findById(7L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CategoryForm form = new CategoryForm();
        form.setName("作品展示");
        form.setSlug("portfolio-new");
        form.setType("post");

        Category updated = service().update(7L, form);

        assertThat(updated.getType()).isEqualTo("project");
    }

    @Test
    void delete_分类存在引用_拒绝删除并提示引用数量() {
        Category category = new Category();
        category.setId(9L);
        when(categoryRepository.findById(9L)).thenReturn(Optional.of(category));
        when(projectRepository.countByCategoryIdIn(List.of(9L))).thenReturn(List.of(count(9L, 1L)));
        when(resourceRepository.countByCategoryIdIn(List.of(9L))).thenReturn(List.of(count(9L, 2L)));
        when(postRepository.countByCategoryIdIn(List.of(9L))).thenReturn(List.of(count(9L, 3L)));

        assertThatThrownBy(() -> service().delete(9L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("该分类被 6 个内容使用，无法删除");

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void delete_分类无引用_删除实体() {
        Category category = new Category();
        category.setId(9L);
        when(categoryRepository.findById(9L)).thenReturn(Optional.of(category));
        when(projectRepository.countByCategoryIdIn(List.of(9L))).thenReturn(List.of());
        when(resourceRepository.countByCategoryIdIn(List.of(9L))).thenReturn(List.of());
        when(postRepository.countByCategoryIdIn(List.of(9L))).thenReturn(List.of());

        service().delete(9L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void findRows_批量聚合分类引用计数() {
        Category first = category(1L, "project");
        Category second = category(2L, "resource");
        when(categoryRepository.findAll(any(Sort.class))).thenReturn(List.of(first, second));
        when(projectRepository.countByCategoryIdIn(List.of(1L, 2L))).thenReturn(List.of(count(1L, 2L)));
        when(resourceRepository.countByCategoryIdIn(List.of(1L, 2L))).thenReturn(List.of(count(2L, 3L)));
        when(postRepository.countByCategoryIdIn(List.of(1L, 2L))).thenReturn(List.of(count(1L, 5L)));

        List<CategoryAdminService.CategoryRow> rows = service().findRows();

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).usage().projects()).isEqualTo(2);
        assertThat(rows.get(0).usage().resources()).isZero();
        assertThat(rows.get(0).usage().posts()).isEqualTo(5);
        assertThat(rows.get(1).usage().projects()).isZero();
        assertThat(rows.get(1).usage().resources()).isEqualTo(3);
        assertThat(rows.get(1).usage().posts()).isZero();
        verify(projectRepository).countByCategoryIdIn(List.of(1L, 2L));
        verify(resourceRepository).countByCategoryIdIn(List.of(1L, 2L));
        verify(postRepository).countByCategoryIdIn(List.of(1L, 2L));
    }

    @Test
    void nameExists_排除自身_走排除id查询() {
        when(categoryRepository.existsByNameAndTypeAndIdNot("学习笔记", "post", 5L)).thenReturn(true);

        boolean exists = service().nameExists(" 学习笔记 ", "post", 5L);

        assertThat(exists).isTrue();
    }

    private CategoryAdminService service() {
        return new CategoryAdminService(
                categoryRepository,
                projectRepository,
                resourceRepository,
                postRepository,
                new AdminSlugService()
        );
    }

    private Category category(Long id, String type) {
        Category category = new Category();
        category.setId(id);
        category.setType(type);
        return category;
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
