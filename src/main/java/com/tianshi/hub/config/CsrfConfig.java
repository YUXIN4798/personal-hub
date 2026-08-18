package com.tianshi.hub.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import java.io.IOException;
import java.util.EnumSet;

@Configuration
public class CsrfConfig {

    @Bean
    public HttpSessionCsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setParameterName("_csrf");
        return repository;
    }

    @Bean
    public FilterRegistrationBean<CsrfFilter> csrfFilter(HttpSessionCsrfTokenRepository csrfTokenRepository) {
        CsrfFilter filter = new CsrfFilter(csrfTokenRepository);
        filter.setRequestHandler(new CsrfTokenRequestAttributeHandler());
        filter.setAccessDeniedHandler(this::handleInvalidCsrfToken);

        FilterRegistrationBean<CsrfFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName("csrfFilter");
        registration.addUrlPatterns("/*");
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        registration.setOrder(0);
        return registration;
    }

    private void handleInvalidCsrfToken(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.access.AccessDeniedException exception
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("""
                <!doctype html>
                <html lang="zh-CN">
                <head><meta charset="UTF-8"><title>表单已过期</title></head>
                <body>
                <main style="font-family: sans-serif; max-width: 40rem; margin: 12vh auto; line-height: 1.8;">
                <h1>表单已过期</h1>
                <p>请返回上一页刷新后重新提交。</p>
                <a href="/admin/login">返回后台登录</a>
                </main>
                </body>
                </html>
                """);
    }
}
