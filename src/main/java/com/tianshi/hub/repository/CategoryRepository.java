package com.tianshi.hub.repository;

import com.tianshi.hub.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByTypeOrderBySortOrderAsc(String type);

    List<Category> findAll(Sort sort);

    boolean existsByNameAndType(String name, String type);

    boolean existsByNameAndTypeAndIdNot(String name, String type, Long id);

    boolean existsBySlugAndType(String slug, String type);

    boolean existsBySlugAndTypeAndIdNot(String slug, String type, Long id);
}
