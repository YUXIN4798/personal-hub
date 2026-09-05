package com.tianshi.hub.repository;

import com.tianshi.hub.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
    long countByCategoryId(Long categoryId);
    @Query("select p.categoryId as id, count(p) as total from Post p where p.categoryId in :categoryIds group by p.categoryId")
    List<UsageCount> countByCategoryIdIn(@Param("categoryIds") List<Long> categoryIds);
    Page<Post> findByStatus(String status, Pageable pageable);

    @Query("""
            select p from Post p
            where p.status = 'published'
              and (p.title like concat('%', :q, '%') escape '\\'
                or p.summary like concat('%', :q, '%') escape '\\'
                or p.content like concat('%', :q, '%') escape '\\')
            order by p.updatedAt desc, p.id desc
            """)
    List<Post> search(@Param("q") String q, Pageable pageable);
}
