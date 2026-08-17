package com.tianshi.hub.config;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class AdminInterceptorTest {

    private final AdminInterceptor adminInterceptor = new AdminInterceptor();

    @Test
    void preHandle_未登录_重定向到登录页() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/projects");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = adminInterceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getRedirectedUrl()).isEqualTo("/admin/login");
    }

    @Test
    void preHandle_已登录_放行() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/projects");
        HttpSession session = request.getSession();
        session.setAttribute(AdminSession.ADMIN_AUTHENTICATED, true);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = adminInterceptor.preHandle(request, response, new Object());

        assertThat(allowed).isTrue();
        assertThat(response.getRedirectedUrl()).isNull();
    }
}
