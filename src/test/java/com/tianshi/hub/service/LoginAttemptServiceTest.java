package com.tianshi.hub.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    @Test
    void recordFailure_连续失败五次_锁定登录() {
        LoginAttemptService service = new LoginAttemptService();

        for (int i = 0; i < 4; i++) {
            service.recordFailure("127.0.0.1:admin");
        }

        assertThat(service.isLocked("127.0.0.1:admin")).isFalse();

        service.recordFailure("127.0.0.1:admin");

        assertThat(service.isLocked("127.0.0.1:admin")).isTrue();
        assertThat(service.remainingMinutes("127.0.0.1:admin")).isBetween(1, 15);
    }

    @Test
    void clear_登录成功_清除失败记录() {
        LoginAttemptService service = new LoginAttemptService();

        for (int i = 0; i < 5; i++) {
            service.recordFailure("127.0.0.1:admin");
        }
        service.clear("127.0.0.1:admin");

        assertThat(service.isLocked("127.0.0.1:admin")).isFalse();
    }
}
