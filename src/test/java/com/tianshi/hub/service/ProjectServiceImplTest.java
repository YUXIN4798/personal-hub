package com.tianshi.hub.service;

import com.tianshi.hub.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Test
    void findPublishedProjects_pageSize越界_夹紧到安全范围并稳定排序() {
        when(projectRepository.findByStatus(eq("published"), any(Pageable.class))).thenReturn(Page.empty());
        ProjectServiceImpl service = new ProjectServiceImpl(projectRepository);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        service.findPublishedProjects(-2, 99);

        verify(projectRepository).findByStatus(eq("published"), captor.capture());
        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(24);
        assertThat(pageable.getSort().getOrderFor("sortOrder")).isEqualTo(Sort.Order.asc("sortOrder"));
        assertThat(pageable.getSort().getOrderFor("id")).isEqualTo(Sort.Order.desc("id"));
    }

    @Test
    void findPublishedProjects_size非法_使用默认分页大小() {
        when(projectRepository.findByStatus(eq("published"), any(Pageable.class))).thenReturn(Page.empty());
        ProjectServiceImpl service = new ProjectServiceImpl(projectRepository);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        service.findPublishedProjects(3, -1);

        verify(projectRepository).findByStatus(eq("published"), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(3);
        assertThat(captor.getValue().getPageSize()).isEqualTo(6);
    }
}
