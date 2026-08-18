package com.tianshi.hub.service;

import com.tianshi.hub.entity.User;
import com.tianshi.hub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPasswordRotationRunnerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Environment environment;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void run_adminPassword不存在_启动失败() {
        when(environment.getProperty("ADMIN_PASSWORD")).thenReturn(null);
        AdminPasswordRotationRunner runner = new AdminPasswordRotationRunner(userRepository, passwordEncoder, environment);

        assertThatThrownBy(() -> runner.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("必须设置 ADMIN_PASSWORD");

        verify(userRepository, never()).findByUsername("admin");
    }

    @Test
    void run_adminPassword与当前Hash不匹配_更新管理员Hash() {
        String configuredPassword = UUID.randomUUID().toString();
        when(environment.getProperty("ADMIN_PASSWORD")).thenReturn(configuredPassword);
        User user = adminUser(passwordEncoder.encode(UUID.randomUUID().toString()));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        AdminPasswordRotationRunner runner = new AdminPasswordRotationRunner(userRepository, passwordEncoder, environment);

        runner.run(new DefaultApplicationArguments());

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).updatePasswordHash(org.mockito.ArgumentMatchers.eq("admin"), hashCaptor.capture());
        assertThat(passwordEncoder.matches(configuredPassword, hashCaptor.getValue())).isTrue();
    }

    @Test
    void run_adminPassword与当前Hash一致_不更新() {
        String configuredPassword = UUID.randomUUID().toString();
        when(environment.getProperty("ADMIN_PASSWORD")).thenReturn(configuredPassword);
        User user = adminUser(passwordEncoder.encode(configuredPassword));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        AdminPasswordRotationRunner runner = new AdminPasswordRotationRunner(userRepository, passwordEncoder, environment);

        runner.run(new DefaultApplicationArguments());

        verify(userRepository, never()).updatePasswordHash(org.mockito.ArgumentMatchers.eq("admin"), org.mockito.ArgumentMatchers.anyString());
    }

    private User adminUser(String passwordHash) {
        User user = new User();
        ReflectionTestUtils.setField(user, "username", "admin");
        ReflectionTestUtils.setField(user, "role", "ADMIN");
        ReflectionTestUtils.setField(user, "passwordHash", passwordHash);
        return user;
    }
}
