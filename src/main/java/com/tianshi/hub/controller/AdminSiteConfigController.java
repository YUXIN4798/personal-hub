package com.tianshi.hub.controller;

import com.tianshi.hub.dto.SiteConfigForm;
import com.tianshi.hub.service.SiteConfigKeys;
import com.tianshi.hub.service.SiteConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/site-config")
public class AdminSiteConfigController {

    private final SiteConfigService siteConfigService;

    public AdminSiteConfigController(SiteConfigService siteConfigService) {
        this.siteConfigService = siteConfigService;
    }

    @GetMapping
    public String editForm(Model model) {
        model.addAttribute("siteConfigForm", buildForm());
        model.addAttribute("pageTitle", "首页设置");
        return "admin/site-config/form";
    }

    @PostMapping
    public String update(@ModelAttribute("siteConfigForm") SiteConfigForm form, RedirectAttributes redirectAttributes) {
        siteConfigService.upsert(SiteConfigKeys.HERO_TITLE, form.getHeroTitle());
        siteConfigService.upsert(SiteConfigKeys.HERO_SUBTITLE, form.getHeroSubtitle());
        siteConfigService.upsert(SiteConfigKeys.PROJECTS_COUNT, form.getProjectsCount());
        siteConfigService.upsert(SiteConfigKeys.NOTES_COUNT, form.getNotesCount());
        siteConfigService.upsert(SiteConfigKeys.RESOURCES_COUNT, form.getResourcesCount());
        redirectAttributes.addFlashAttribute("successMessage", "首页设置已保存");
        return "redirect:/admin/site-config";
    }

    private SiteConfigForm buildForm() {
        SiteConfigForm form = new SiteConfigForm();
        form.setHeroTitle(siteConfigService.get(SiteConfigKeys.HERO_TITLE, SiteConfigKeys.DEFAULT_HERO_TITLE));
        form.setHeroSubtitle(siteConfigService.get(SiteConfigKeys.HERO_SUBTITLE, SiteConfigKeys.DEFAULT_HERO_SUBTITLE));
        form.setProjectsCount(String.valueOf(siteConfigService.getInt(SiteConfigKeys.PROJECTS_COUNT, SiteConfigKeys.DEFAULT_SECTION_COUNT)));
        form.setNotesCount(String.valueOf(siteConfigService.getInt(SiteConfigKeys.NOTES_COUNT, SiteConfigKeys.DEFAULT_SECTION_COUNT)));
        form.setResourcesCount(String.valueOf(siteConfigService.getInt(SiteConfigKeys.RESOURCES_COUNT, SiteConfigKeys.DEFAULT_SECTION_COUNT)));
        return form;
    }
}
