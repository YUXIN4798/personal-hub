package com.tianshi.hub.repository;

import com.tianshi.hub.entity.SiteConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteConfigRepository extends JpaRepository<SiteConfig, Long> {

    Optional<SiteConfig> findByConfigKey(String configKey);
}
