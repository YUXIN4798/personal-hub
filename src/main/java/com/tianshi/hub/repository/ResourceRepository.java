package com.tianshi.hub.repository;

import com.tianshi.hub.entity.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    Page<Resource> findByVisibility(String visibility, Pageable pageable);

    Page<Resource> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Resource> findByVisibilityAndCategoryId(String visibility, Long categoryId, Pageable pageable);
}
