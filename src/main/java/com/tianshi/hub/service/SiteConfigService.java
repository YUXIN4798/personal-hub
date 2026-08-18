package com.tianshi.hub.service;

public interface SiteConfigService {

    String get(String key, String defaultValue);

    int getInt(String key, int defaultValue);

    void upsert(String key, String value);
}
