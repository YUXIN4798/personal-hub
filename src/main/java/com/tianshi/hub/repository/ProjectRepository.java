package com.tianshi.hub.repository;

import com.tianshi.hub.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    long countByCategoryId(Long categoryId);

    @Query("select p.categoryId as id, count(p) as total from Project p where p.categoryId in :categoryIds group by p.categoryId")
    List<UsageCount> countByCategoryIdIn(@Param("categoryIds") List<Long> categoryIds);

    Page<Project> findByStatus(String status, Pageable pageable);

    Page<Project> findByStatusAndFeatured(String status, boolean featured, Pageable pageable);

    Page<Project> findByStatusAndIdNotIn(String status, List<Long> ids, Pageable pageable);
}
