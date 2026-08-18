package com.tianshi.hub.controller;

import com.tianshi.hub.dto.SiteConfigForm;
import com.tianshi.hub.service.SiteConfigKeys;
import com.tianshi.hub.service.SiteConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class AdminSiteConfigControllerTest {

    @Mock
    private SiteConfigService siteConfigService;

    @Test
    void editForm_读取配置_回显到表单模型() throws Exception {
        when(siteConfigService.get(SiteConfigKeys.HERO_TITLE, SiteConfigKeys.DEFAULT_HERO_TITLE)).thenReturn("标题");
        when(siteConfigService.get(SiteConfigKeys.HERO_SUBTITLE, SiteConfigKeys.DEFAULT_HERO_SUBTITLE)).thenReturn("副标题");
        when(siteConfigService.getInt(SiteConfigKeys.PROJECTS_COUNT, SiteConfigKeys.DEFAULT_SECTION_COUNT)).thenReturn(4);
        when(siteConfigService.getInt(SiteConfigKeys.NOTES_COUNT, SiteConfigKeys.DEFAULT_SECTION_COUNT)).thenReturn(2);
        when(siteConfigService.getInt(SiteConfigKeys.RESOURCES_COUNT, SiteConfigKeys.DEFAULT_SECTION_COUNT)).thenReturn(1);

        MvcResult result = mockMvc().perform(get("/admin/site-config"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/site-config/form"))
                .andExpect(model().attributeExists("siteConfigForm"))
                .andReturn();

        SiteConfigForm form = (SiteConfigForm) result.getModelAndView().getModel().get("siteConfigForm");
        assertThat(form.getHeroTitle()).isEqualTo("标题");
        assertThat(form.getProjectsCount()).isEqualTo("4");
    }

    @Test
    void update_提交表单_保存后重定向并提示成功() throws Exception {
        mockMvc().perform(post("/admin/site-config")
                        .param("heroTitle", "新标题")
                        .param("heroSubtitle", "新副标题")
                        .param("projectsCount", "5")
                        .param("notesCount", "4")
                        .param("resourcesCount", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/site-config"))
                .andExpect(flash().attribute("successMessage", "首页设置已保存"));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(siteConfigService, times(5)).upsert(keyCaptor.capture(), valueCaptor.capture());
        assertThat(keyCaptor.getAllValues()).containsExactly(
                SiteConfigKeys.HERO_TITLE,
                SiteConfigKeys.HERO_SUBTITLE,
                SiteConfigKeys.PROJECTS_COUNT,
                SiteConfigKeys.NOTES_COUNT,
                SiteConfigKeys.RESOURCES_COUNT
        );
        assertThat(valueCaptor.getAllValues()).containsExactly("新标题", "新副标题", "5", "4", "2");
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(new AdminSiteConfigController(siteConfigService)).build();
    }
}
