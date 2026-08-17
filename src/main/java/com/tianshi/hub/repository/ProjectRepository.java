package com.tianshi.hub.repository;

import com.tianshi.hub.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    List<Project> findByStatusOrderBySortOrderAsc(String status);

    Page<Project> findByStatus(String status, Pageable pageable);
}
