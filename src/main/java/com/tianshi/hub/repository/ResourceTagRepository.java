package com.tianshi.hub.repository;

import com.tianshi.hub.entity.ResourceTag;
import com.tianshi.hub.entity.ResourceTagId;
import com.tianshi.hub.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceTagRepository extends JpaRepository<ResourceTag, ResourceTagId> {

    void deleteByResource_Id(Long resourceId);

    long countByTag_Id(Long tagId);

    @Query("select rt.tag.id as id, count(rt) as total from ResourceTag rt where rt.tag.id in :tagIds group by rt.tag.id")
    List<UsageCount> countByTagIdIn(@Param("tagIds") List<Long> tagIds);

    @Query("select rt.tag from ResourceTag rt where rt.resource.id = :resourceId order by rt.tag.name asc")
    List<Tag> findTagsByResourceId(@Param("resourceId") Long resourceId);

    @Query("""
            select rt from ResourceTag rt
            join fetch rt.resource
            join fetch rt.tag
            where rt.resource.id in :resourceIds
            order by rt.tag.name asc
            """)
    List<ResourceTag> findByResource_IdInOrderByTag_NameAsc(@Param("resourceIds") List<Long> resourceIds);
}
