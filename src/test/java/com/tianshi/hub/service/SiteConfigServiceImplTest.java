package com.tianshi.hub.service;

import com.tianshi.hub.entity.SiteConfig;
import com.tianshi.hub.repository.SiteConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiteConfigServiceImplTest {

    @Mock
    private SiteConfigRepository siteConfigRepository;

    @Test
    void get_配置不存在_返回默认值() {
        when(siteConfigRepository.findByConfigKey("homepage.hero.title")).thenReturn(java.util.Optional.empty());
        SiteConfigService service = new SiteConfigServiceImpl(siteConfigRepository);

        String value = service.get("homepage.hero.title", "默认标题");

        assertThat(value).isEqualTo("默认标题");
    }

    @Test
    void getInt_非法数字_返回默认值() {
        SiteConfig siteConfig = new SiteConfig();
        siteConfig.setConfigKey("homepage.projects.count");
        siteConfig.setConfigValue("abc");
        when(siteConfigRepository.findByConfigKey("homepage.projects.count")).thenReturn(java.util.Optional.of(siteConfig));
        SiteConfigService service = new SiteConfigServiceImpl(siteConfigRepository);

        int value = service.getInt("homepage.projects.count", 3);

        assertThat(value).isEqualTo(3);
    }

    @Test
    void upsert_已有配置_更新并保存() {
        SiteConfig siteConfig = new SiteConfig();
        siteConfig.setConfigKey("homepage.notes.count");
        siteConfig.setConfigValue("2");
        when(siteConfigRepository.findByConfigKey("homepage.notes.count")).thenReturn(java.util.Optional.of(siteConfig));
        SiteConfigService service = new SiteConfigServiceImpl(siteConfigRepository);

        service.upsert("homepage.notes.count", "5");

        ArgumentCaptor<SiteConfig> captor = ArgumentCaptor.forClass(SiteConfig.class);
        verify(siteConfigRepository).save(captor.capture());
        assertThat(captor.getValue().getConfigKey()).isEqualTo("homepage.notes.count");
        assertThat(captor.getValue().getConfigValue()).isEqualTo("5");
    }
}
