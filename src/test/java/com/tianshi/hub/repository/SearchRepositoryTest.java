package com.tianshi.hub.repository;

import com.tianshi.hub.entity.Post;
import com.tianshi.hub.entity.Project;
import com.tianshi.hub.entity.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SearchRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Test
    void projectSearch_匹配标题摘要和描述_并限制数量() {
        Project matched = new Project();
        matched.setTitle("Spring Search");
        matched.setSlug("spring-search");
        matched.setSummary("alpha");
        matched.setDescription("beta");
        matched.setStatus("published");
        matched.setSortOrder(1);
        matched.setFeatured(false);
        touch(matched);
        projectRepository.save(matched);

        Project hidden = new Project();
        hidden.setTitle("Hidden Search");
        hidden.setSlug("hidden-search");
        hidden.setSummary("alpha");
        hidden.setDescription("beta");
        hidden.setStatus("draft");
        hidden.setSortOrder(2);
        hidden.setFeatured(false);
        touch(hidden);
        projectRepository.save(hidden);

        assertThat(projectRepository.search("Search", PageRequest.of(0, 20))).extracting(Project::getSlug)
                .containsExactly("spring-search");
    }

    @Test
    void postSearch_特殊字符关键词_不报错且可返回结果() {
        Post post = new Post();
        post.setTitle("100% search");
        post.setSlug("special-post");
        post.setSummary("safe _ query");
        post.setContent("ignored");
        post.setStatus("published");
        touch(post);
        postRepository.save(post);

        assertThat(postRepository.search("%", PageRequest.of(0, 20))).extracting(Post::getSlug)
                .contains("special-post");
        assertThat(postRepository.search("_", PageRequest.of(0, 20))).extracting(Post::getSlug)
                .contains("special-post");
        assertThat(postRepository.search("'", PageRequest.of(0, 20))).isEmpty();
    }

    @Test
    void resourceSearch_匹配描述并忽略非公开资源() {
        Resource visible = new Resource();
        visible.setTitle("Resource Search");
        visible.setSlug("resource-search");
        visible.setSummary("alpha");
        visible.setDescription("gamma");
        visible.setUrl("https://example.com");
        visible.setType("link");
        visible.setFileSize(0);
        visible.setVersion("v1");
        visible.setDownloadCount(0);
        visible.setVisibility("public");
        touch(visible);
        resourceRepository.save(visible);

        Resource hidden = new Resource();
        hidden.setTitle("Hidden Resource");
        hidden.setSlug("hidden-resource");
        hidden.setSummary("alpha");
        hidden.setDescription("gamma");
        hidden.setUrl("https://example.com");
        hidden.setType("link");
        hidden.setFileSize(0);
        hidden.setVersion("v1");
        hidden.setDownloadCount(0);
        hidden.setVisibility("private");
        touch(hidden);
        resourceRepository.save(hidden);

        assertThat(resourceRepository.search("gamma", PageRequest.of(0, 20))).extracting(Resource::getSlug)
                .containsExactly("resource-search");
    }

    private void touch(Object entity) {
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.now());
    }
}
