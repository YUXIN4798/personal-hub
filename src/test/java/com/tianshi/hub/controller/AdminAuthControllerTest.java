package com.tianshi.hub.controller;

import com.tianshi.hub.config.AdminSession;
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

    private MockMvc mockMvc(LoginAttemptService loginAttemptService) {
        return MockMvcBuilders.standaloneSetup(new AdminAuthController(adminAuthService, loginAttemptService)).build();
    }
}
