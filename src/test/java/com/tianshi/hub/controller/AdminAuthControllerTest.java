package com.tianshi.hub.controller;

import com.tianshi.hub.config.AdminSession;
import com.tianshi.hub.config.AppProperties;
import com.tianshi.hub.service.AdminAuthService;
import com.tianshi.hub.service.LoginAttemptService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminAuthControllerTest {

    @Mock
    private AdminAuthService adminAuthService;

    @Test
    void login_认证成功_轮换SessionId并写入登录态() throws Exception {
        LoginAttemptService loginAttemptService = new LoginAttemptService();
        MockMvc mockMvc = mockMvc(loginAttemptService);
        MockHttpSession session = new MockHttpSession();
        String before = session.getId();
        when(adminAuthService.authenticate("admin", "secret")).thenReturn(true);

        MvcResult result = mockMvc.perform(post("/admin/login")
                        .session(session)
                        .param("username", "admin")
                        .param("password", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/projects"))
                .andReturn();

        MockHttpSession after = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(after).isNotNull();
        assertThat(after.getId()).isNotEqualTo(before);
        assertThat(after.getAttribute(AdminSession.ADMIN_AUTHENTICATED)).isEqualTo(true);
    }

    @Test
    void login_连续失败五次后_第六次被限流() throws Exception {
        LoginAttemptService loginAttemptService = new LoginAttemptService();
        MockMvc mockMvc = mockMvc(loginAttemptService);
        when(adminAuthService.authenticate("admin", "bad")).thenReturn(false);

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/admin/login").param("username", "admin").param("password", "bad"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/login"));
        }

        mockMvc.perform(post("/admin/login").param("username", "admin").param("password", "bad"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"))
                .andExpect(flash().attribute("loginError", "登录失败次数过多，请 15 分钟后再试"));

        verify(adminAuthService, org.mockito.Mockito.times(5)).authenticate("admin", "bad");
    }

    @Test
    void login_可信代理_使用Xff左一作为限流客户端Ip() throws Exception {
        LoginAttemptService loginAttemptService = new LoginAttemptService();
        AppProperties properties = new AppProperties();
        properties.getSecurity().setTrustedProxies(java.util.List.of("127.0.0.1"));
        MockMvc mockMvc = mockMvc(loginAttemptService, properties);
        when(adminAuthService.authenticate("admin", "bad")).thenReturn(false);

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/admin/login")
                            .with(request -> {
                                request.setRemoteAddr("127.0.0.1");
                                return request;
                            })
                            .header("X-Forwarded-For", "203.0.113.8, 127.0.0.1")
                            .param("username", "admin")
                            .param("password", "bad"))
                    .andExpect(status().is3xxRedirection());
        }

        mockMvc.perform(post("/admin/login")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        })
                        .header("X-Forwarded-For", "203.0.113.9, 127.0.0.1")
                        .param("username", "admin")
                        .param("password", "bad"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"))
                .andExpect(flash().attribute("loginError", "用户名或密码错误"));
    }

    @Test
    void login_非可信代理_忽略Xff避免伪造客户端Ip() throws Exception {
        LoginAttemptService loginAttemptService = new LoginAttemptService();
        MockMvc mockMvc = mockMvc(loginAttemptService);
        when(adminAuthService.authenticate("admin", "bad")).thenReturn(false);

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/admin/login")
                            .with(request -> {
                                request.setRemoteAddr("198.51.100.7");
                                return request;
                            })
                            .header("X-Forwarded-For", "203.0.113." + i)
                            .param("username", "admin")
                            .param("password", "bad"))
                    .andExpect(status().is3xxRedirection());
        }

        mockMvc.perform(post("/admin/login")
                        .with(request -> {
                            request.setRemoteAddr("198.51.100.7");
                            return request;
                        })
                        .header("X-Forwarded-For", "203.0.113.99")
                        .param("username", "admin")
                        .param("password", "bad"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"))
                .andExpect(flash().attribute("loginError", "登录失败次数过多，请 15 分钟后再试"));
    }

    private MockMvc mockMvc(LoginAttemptService loginAttemptService) {
        return mockMvc(loginAttemptService, new AppProperties());
    }

    private MockMvc mockMvc(LoginAttemptService loginAttemptService, AppProperties appProperties) {
        return MockMvcBuilders.standaloneSetup(
                new AdminAuthController(adminAuthService, loginAttemptService, appProperties)
        ).build();
    }
}
