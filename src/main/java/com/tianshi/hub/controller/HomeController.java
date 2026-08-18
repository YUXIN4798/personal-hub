package com.tianshi.hub.controller;

import com.tianshi.hub.service.HomeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("homeContent", homeService.getHomeContent());
        model.addAttribute("pageTitle", "首页");
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
