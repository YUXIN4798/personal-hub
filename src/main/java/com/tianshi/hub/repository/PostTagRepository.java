package com.tianshi.hub.repository;

import com.tianshi.hub.entity.PostTag;
import com.tianshi.hub.entity.PostTagId;
import com.tianshi.hub.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag, PostTagId> {

    void deleteByPost_Id(Long postId);

    @Query("select pt.tag from PostTag pt where pt.post.id = :postId order by pt.tag.name asc")
    List<Tag> findTagsByPostId(@Param("postId") Long postId);

    List<PostTag> findByPost_IdInOrderByTag_NameAsc(List<Long> postIds);
}
