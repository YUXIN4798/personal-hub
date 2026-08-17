-- 仅本地开发，生产必须改：admin123
INSERT INTO users (username, email, password_hash, role)
VALUES ('admin', '479819599@qq.com', '$2b$10$IVBd25j.EFXBxbQNVWyXweW7ijXyAz3BauBMQrbiM8Q/CtScUGiQm', 'ADMIN')
ON DUPLICATE KEY UPDATE username = username;
