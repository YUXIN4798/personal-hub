-- V7: 管理员密码按铭轩要求轮换（2026-08-17）
UPDATE users SET password_hash = '$2a$10$9L/G0VzJtHFC2yepPDE1KeoHIBpdyP6wBL7YT8GVLSRZIuHbnJtwa' WHERE username = 'admin';
