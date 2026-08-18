package com.tianshi.hub.service;

import com.tianshi.hub.entity.User;
import com.tianshi.hub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void authenticate_管理员密码匹配_返回True() {
        User admin = user("admin", "ADMIN", passwordEncoder.encode("secret"));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        boolean authenticated = new AdminAuthService(userRepository, passwordEncoder)
                .authenticate("admin", "secret");

        assertThat(authenticated).isTrue();
    }

    @Test
    void authenticate_非管理员或密码错误_返回False() {
        User normalUser = user("user", "USER", passwordEncoder.encode("secret"));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(normalUser));

        boolean authenticated = new AdminAuthService(userRepository, passwordEncoder)
                .authenticate("user", "secret");

        assertThat(authenticated).isFalse();
    }

    @Test
    void authenticate_用户名或密码为空_不查询数据库() {
        AdminAuthService service = new AdminAuthService(userRepository, passwordEncoder);

        assertThat(service.authenticate(null, "secret")).isFalse();
        assertThat(service.authenticate("admin", null)).isFalse();
        verify(userRepository, never()).findByUsername(org.mockito.ArgumentMatchers.anyString());
    }

    private User user(String username, String role, String passwordHash) {
        User user = new User();
        ReflectionTestUtils.setField(user, "username", username);
        ReflectionTestUtils.setField(user, "role", role);
        ReflectionTestUtils.setField(user, "passwordHash", passwordHash);
        return user;
    }
}
