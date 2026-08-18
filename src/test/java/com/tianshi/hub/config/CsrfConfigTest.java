package com.tianshi.hub.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class CsrfConfigTest {

    @Test
    void csrfFilter_post缺少Token_返回友好403且不放行() throws Exception {
        CsrfConfig config = new CsrfConfig();
        HttpSessionCsrfTokenRepository repository = config.csrfTokenRepository();
        CsrfFilter filter = config.csrfFilter(repository).getFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (servletRequest, servletResponse) -> chainCalled.set(true);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("表单已过期");
        assertThat(chainCalled).isFalse();
    }
}
