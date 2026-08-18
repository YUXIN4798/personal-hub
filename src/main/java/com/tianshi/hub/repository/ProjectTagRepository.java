package com.tianshi.hub.repository;

import com.tianshi.hub.entity.ProjectTag;
import com.tianshi.hub.entity.ProjectTagId;
import com.tianshi.hub.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectTagRepository extends JpaRepository<ProjectTag, ProjectTagId> {

    void deleteByProject_Id(Long projectId);

    long countByTag_Id(Long tagId);

    @Query("select pt.tag.id as id, count(pt) as total from ProjectTag pt where pt.tag.id in :tagIds group by pt.tag.id")
    List<UsageCount> countByTagIdIn(@Param("tagIds") List<Long> tagIds);

    @Query("select pt.tag from ProjectTag pt where pt.project.id = :projectId order by pt.tag.name asc")
    List<Tag> findTagsByProjectId(@Param("projectId") Long projectId);

    @Query("""
            select pt from ProjectTag pt
            join fetch pt.project
            join fetch pt.tag
            where pt.project.id in :projectIds
            order by pt.tag.name asc
            """)
    List<ProjectTag> findByProject_IdInOrderByTag_NameAsc(@Param("projectIds") List<Long> projectIds);
}
