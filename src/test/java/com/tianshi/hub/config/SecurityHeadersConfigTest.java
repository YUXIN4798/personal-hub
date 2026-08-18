package com.tianshi.hub.config;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityHeadersConfigTest {

    @Test
    void securityHeadersFilter_所有响应带安全头() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .addFilters(new SecurityHeadersConfig().securityHeadersFilter().getFilter())
                .build();

        mockMvc.perform(get("/headers-test"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")))
                .andExpect(header().string("Content-Security-Policy", containsString("style-src 'self' 'unsafe-inline'")))
                .andExpect(header().string("Content-Security-Policy", containsString("script-src 'self' 'unsafe-inline'")));

        mockMvc.perform(get("/uploads/missing.txt"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Controller
    private static class TestController {
        @GetMapping("/headers-test")
        String headersTest() {
            return "ok";
        }
    }
}
