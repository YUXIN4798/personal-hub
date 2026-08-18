package com.tianshi.hub.repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TagAssociationRepositoryTest {

    @Test
    void projectTag_批量列表查询_joinFetch父实体和标签() throws Exception {
        assertJoinFetchQuery(ProjectTagRepository.class, "findByProject_IdInOrderByTag_NameAsc");
    }

    @Test
    void resourceTag_批量列表查询_joinFetch父实体和标签() throws Exception {
        assertJoinFetchQuery(ResourceTagRepository.class, "findByResource_IdInOrderByTag_NameAsc");
    }

    @Test
    void postTag_批量列表查询_joinFetch父实体和标签() throws Exception {
        assertJoinFetchQuery(PostTagRepository.class, "findByPost_IdInOrderByTag_NameAsc");
    }

    private void assertJoinFetchQuery(Class<?> repositoryClass, String methodName) throws NoSuchMethodException {
        Method method = repositoryClass.getMethod(methodName, List.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(query).isNotNull();
        assertThat(query.value()).containsIgnoringWhitespaces("join fetch");
        assertThat(query.value()).contains(".tag");
    }
}
