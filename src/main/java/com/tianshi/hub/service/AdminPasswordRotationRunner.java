package com.tianshi.hub.service;

import com.tianshi.hub.entity.User;
import com.tianshi.hub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminPasswordRotationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminPasswordRotationRunner.class);
    private static final String ADMIN_PASSWORD_ENV = "ADMIN_PASSWORD";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_ROLE = "ADMIN";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Environment environment;

    public AdminPasswordRotationRunner(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            Environment environment
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String configuredPassword = environment.getProperty(ADMIN_PASSWORD_ENV);
        if (configuredPassword == null || configuredPassword.isBlank()) {
            log.info("ADMIN_PASSWORD 未设置，跳过管理员密码自动轮换");
            return;
        }

        userRepository.findByUsername(ADMIN_USERNAME)
                .filter(user -> ADMIN_ROLE.equals(user.getRole()))
                .ifPresentOrElse(
                        user -> rotateIfChanged(user, configuredPassword),
                        () -> log.warn("未找到 admin 管理员账号，跳过密码自动轮换")
                );
    }

    private void rotateIfChanged(User user, String configuredPassword) {
        if (passwordEncoder.matches(configuredPassword, user.getPasswordHash())) {
            log.info("ADMIN_PASSWORD 与当前管理员密码一致，跳过密码自动轮换");
            return;
        }
        userRepository.updatePasswordHash(user.getUsername(), passwordEncoder.encode(configuredPassword));
        log.info("管理员密码已根据 ADMIN_PASSWORD 自动轮换");
    }
}
