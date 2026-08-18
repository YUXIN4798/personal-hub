package com.tianshi.hub.service;

import com.tianshi.hub.dto.HomeContent;
import com.tianshi.hub.entity.Post;
import com.tianshi.hub.entity.Project;
import com.tianshi.hub.entity.Resource;
import com.tianshi.hub.repository.PostRepository;
import com.tianshi.hub.repository.ProjectRepository;
import com.tianshi.hub.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private SiteConfigService siteConfigService;

    @Test
    void getHomeContent_精选优先并回填最新内容() {
        when(siteConfigService.get(SiteConfigKeys.HERO_TITLE, SiteConfigKeys.DEFAULT_HERO_TITLE)).thenReturn("自定义标题");
        when(siteConfigService.get(SiteConfigKeys.HERO_SUBTITLE, SiteConfigKeys.DEFAULT_HERO_SUBTITLE)).thenReturn("自定义副标题");
        when(siteConfigService.getInt(SiteConfigKeys.PROJECTS_COUNT, SiteConfigKeys.DEFAULT_SECTION_COUNT)).thenReturn(3);
        when(siteConfigService.getInt(SiteConfigKeys.NOTES_COUNT, SiteConfigKeys.DEFAULT_SECTION_COUNT)).thenReturn(3);
        when(siteConfigService.getInt(SiteConfigKeys.RESOURCES_COUNT, SiteConfigKeys.DEFAULT_SECTION_COUNT)).thenReturn(3);

        Project featuredNew = project("featured-new", "精选新", 1, LocalDateTime.of(2026, 8, 18, 10, 0), true);
        Project featuredOld = project("featured-old", "精选旧", 2, LocalDateTime.of(2026, 8, 17, 10, 0), true);
        Project fallback = project("fallback", "回填项", 3, LocalDateTime.of(2026, 8, 19, 10, 0), false);
        when(projectRepository.findByStatusAndFeatured(eq("published"), eq(true), any(Pageable.class)))
                .thenReturn(page(featuredNew, featuredOld));
        when(projectRepository.findByStatusAndIdNotIn(eq("published"), eq(List.of(1L, 2L)), any(Pageable.class)))
                .thenReturn(page(fallback));

        Post post = post("note-1", "笔记 1", LocalDateTime.of(2026, 8, 18, 9, 0));
        when(postRepository.findByStatus(eq("published"), any(Pageable.class))).thenReturn(page(post));

        Resource resource = resource("resource-1", "资源 1", "file");
        when(resourceRepository.findByVisibility(eq("public"), any(Pageable.class))).thenReturn(page(resource));

        HomeService service = new HomeServiceImpl(projectRepository, postRepository, resourceRepository, siteConfigService);
        var content = service.getHomeContent();

        assertThat(content.heroTitle()).isEqualTo("自定义标题");
        assertThat(content.heroSubtitle()).isEqualTo("自定义副标题");
        assertThat(content.projects()).extracting(HomeContent.ProjectItem::title)
                .containsExactly("精选新", "精选旧", "回填项");
        assertThat(content.projects()).extracting(HomeContent.ProjectItem::summary)
                .containsExactly("精选新摘要", "精选旧摘要", "回填项摘要");
        assertThat(content.notes()).extracting(HomeContent.NoteItem::title).containsExactly("笔记 1");
        assertThat(content.resources()).extracting(HomeContent.ResourceItem::title).containsExactly("资源 1");

        ArgumentCaptor<Pageable> projectCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(projectRepository).findByStatusAndFeatured(eq("published"), eq(true), projectCaptor.capture());
        assertThat(projectCaptor.getValue().getPageSize()).isEqualTo(3);
    }

    @Test
    void getHomeContent_配置覆盖默认值_使用自定义数量() {
        when(siteConfigService.get(SiteConfigKeys.HERO_TITLE, SiteConfigKeys.DEFAULT_HERO_TITLE)).thenReturn(SiteConfigKeys.DEFAULT_HERO_TITLE);
        when(siteConfigService.get(SiteConfigKeys.HERO_SUBTITLE, SiteConfigKeys.DEFAULT_HERO_SUBTITLE)).thenReturn(SiteConfigKeys.DEFAULT_HERO_SUBTITLE);
        when(siteConfigService.getInt(SiteConfigKeys.PROJECTS_COUNT, SiteConfigKeys.DEFAULT_SECTION_COUNT)).thenReturn(2);
        when(siteConfigService.getInt(SiteConfigKeys.NOTES_COUNT, SiteConfigKeys.DEFAULT_SECTION_COUNT)).thenReturn(1);
        when(siteConfigService.getInt(SiteConfigKeys.RESOURCES_COUNT, SiteConfigKeys.DEFAULT_SECTION_COUNT)).thenReturn(0);
        when(projectRepository.findByStatusAndFeatured(eq("published"), eq(true), any(Pageable.class))).thenReturn(Page.empty());
        when(projectRepository.findByStatus(eq("published"), any(Pageable.class))).thenReturn(page(project("p-1", "项目 1", 1, LocalDateTime.now(), false), project("p-2", "项目 2", 2, LocalDateTime.now().minusDays(1), false)));
        when(postRepository.findByStatus(eq("published"), any(Pageable.class))).thenReturn(page(post("n-1", "笔记 1", LocalDateTime.now())));
        HomeService service = new HomeServiceImpl(projectRepository, postRepository, resourceRepository, siteConfigService);

        var content = service.getHomeContent();

        assertThat(content.projects()).hasSize(2);
        assertThat(content.notes()).hasSize(1);
        assertThat(content.resources()).isEmpty();
    }

    private Page<Project> page(Project... projects) {
        return new PageImpl<>(List.of(projects));
    }

    private Page<Post> page(Post... posts) {
        return new PageImpl<>(List.of(posts));
    }

    private Page<Resource> page(Resource... resources) {
        return new PageImpl<>(List.of(resources));
    }

    private Project project(String slug, String title, int sortOrder, LocalDateTime createdAt, boolean featured) {
        Project project = new Project();
        project.setTitle(title);
        project.setSlug(slug);
        project.setSummary(title + "摘要");
        project.setStatus("published");
        project.setSortOrder(sortOrder);
        project.setFeatured(featured);
        org.springframework.test.util.ReflectionTestUtils.setField(project, "id", (long) sortOrder);
        org.springframework.test.util.ReflectionTestUtils.setField(project, "createdAt", createdAt);
        org.springframework.test.util.ReflectionTestUtils.setField(project, "publishedAt", createdAt);
        return project;
    }

    private Post post(String slug, String title, LocalDateTime publishedAt) {
        Post post = new Post();
        post.setTitle(title);
        post.setSlug(slug);
        post.setStatus("published");
        post.setPublishedAt(publishedAt);
        org.springframework.test.util.ReflectionTestUtils.setField(post, "id", 1L);
        return post;
    }

    private Resource resource(String slug, String title, String type) {
        Resource resource = new Resource();
        resource.setTitle(title);
        resource.setSlug(slug);
        resource.setType(type);
        resource.setVisibility("public");
        return resource;
    }
}
