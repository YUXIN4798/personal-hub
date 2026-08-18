package com.tianshi.hub.controller;

import com.tianshi.hub.config.AdminSession;
import com.tianshi.hub.service.AdminAuthService;
import com.tianshi.hub.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthController.class);

    private final AdminAuthService adminAuthService;
    private final LoginAttemptService loginAttemptService;

    public AdminAuthController(AdminAuthService adminAuthService, LoginAttemptService loginAttemptService) {
        this.adminAuthService = adminAuthService;
        this.loginAttemptService = loginAttemptService;
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("pageTitle", "后台登录");
        return "admin/login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        String attemptKey = attemptKey(request, username);
        if (loginAttemptService.isLocked(attemptKey)) {
            redirectAttributes.addFlashAttribute("loginError",
                    "登录失败次数过多，请 " + loginAttemptService.remainingMinutes(attemptKey) + " 分钟后再试");
            return "redirect:/admin/login";
        }
        if (adminAuthService.authenticate(username, password)) {
            HttpSession session = request.getSession();
            request.changeSessionId();
            session.setAttribute(AdminSession.ADMIN_AUTHENTICATED, true);
            loginAttemptService.clear(attemptKey);
            log.info("Admin user logged in: {}", username);
            return "redirect:/admin/projects";
        }
        loginAttemptService.recordFailure(attemptKey);
        log.warn("Admin login failed for username: {}", username);
        redirectAttributes.addFlashAttribute("loginError", "用户名或密码错误");
        return "redirect:/admin/login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    private String attemptKey(HttpServletRequest request, String username) {
        String remoteAddress = request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
        String normalizedUsername = username == null ? "" : username.trim().toLowerCase();
        return remoteAddress + ":" + normalizedUsername;
    }
}
