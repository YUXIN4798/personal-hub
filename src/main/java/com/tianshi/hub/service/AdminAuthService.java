package com.tianshi.hub.service;

import com.tianshi.hub.entity.User;
import com.tianshi.hub.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminAuthService {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$7EqJtq98hPqEX7fNZaFWoOhi96dvzXuBB88Y0GuXjaMnJsU07C7bS";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminAuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        return userRepository.findByUsername(username)
                .filter(user -> ADMIN_ROLE.equals(user.getRole()))
                .map(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                .orElseGet(() -> {
                    passwordEncoder.matches(password, DUMMY_PASSWORD_HASH);
                    return false;
                });
    }
}
