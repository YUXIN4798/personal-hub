package com.tianshi.hub.service;

import com.tianshi.hub.entity.SiteConfig;
import com.tianshi.hub.repository.SiteConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SiteConfigServiceImpl implements SiteConfigService {

    private final SiteConfigRepository siteConfigRepository;

    public SiteConfigServiceImpl(SiteConfigRepository siteConfigRepository) {
        this.siteConfigRepository = siteConfigRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public String get(String key, String defaultValue) {
        return siteConfigRepository.findByConfigKey(key)
                .map(SiteConfig::getConfigValue)
                .filter(value -> value != null && !value.isBlank())
                .orElse(defaultValue);
    }

    @Override
    @Transactional(readOnly = true)
    public int getInt(String key, int defaultValue) {
        String value = get(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    @Override
    @Transactional
    public void upsert(String key, String value) {
        SiteConfig siteConfig = siteConfigRepository.findByConfigKey(key).orElseGet(SiteConfig::new);
        siteConfig.setConfigKey(key);
        siteConfig.setConfigValue(value == null ? "" : value.trim());
        siteConfigRepository.save(siteConfig);
    }
}
