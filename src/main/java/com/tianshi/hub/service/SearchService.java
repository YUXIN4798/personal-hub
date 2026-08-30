package com.tianshi.hub.service;

import com.tianshi.hub.entity.Post;
import com.tianshi.hub.entity.Project;
import com.tianshi.hub.entity.Resource;

import java.util.List;

public interface SearchService {

    SearchResults search(String query);

    record SearchResults(List<Project> projects, List<Post> posts, List<Resource> resources) {
    }
}
