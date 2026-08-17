package com.tianshi.hub.service;

import com.tianshi.hub.config.AppProperties;
import com.tianshi.hub.exception.FileStorageException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    public static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path uploadRoot;
    private final long maxSizeBytes;

    public FileStorageService(AppProperties properties) {
        this.uploadRoot = Path.of(properties.getUploadDir()).toAbsolutePath().normalize();
        this.maxSizeBytes = properties.getUpload().getMaxSize().toBytes();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException exception) {
            throw new FileStorageException("无法创建上传目录", exception);
        }
    }

    public String store(MultipartFile file) {
        return store(file, IMAGE_EXTENSIONS);
    }

    public String store(MultipartFile file, Set<String> allowedExtensions) {
        validate(file, allowedExtensions);
        String extension = extractExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "." + extension;
        Path target = uploadRoot.resolve(fileName).normalize();
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new FileStorageException("保存文件失败", exception);
        }
        return "/uploads/" + fileName;
    }

    public Path resolve(String relativePath) {
        String normalized = normalizeRelativePath(relativePath);
        Path resolved = uploadRoot.resolve(normalized).normalize();
        if (!resolved.startsWith(uploadRoot)) {
            throw new FileStorageException("非法文件路径");
        }
        return resolved;
    }

    public void delete(String relativePath) {
        Path path = resolve(relativePath);
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new FileStorageException("删除文件失败", exception);
        }
    }

    public List<StoredFile> listImageFiles() {
        if (!Files.exists(uploadRoot)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(uploadRoot)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(this::isImageFile)
                    .sorted(Comparator.comparingLong(this::lastModifiedEpoch).reversed())
                    .map(this::toStoredFile)
                    .toList();
        } catch (IOException exception) {
            throw new FileStorageException("读取上传目录失败", exception);
        }
    }

    private void validate(MultipartFile file, Set<String> allowedExtensions) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("文件不能为空");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new FileStorageException("文件不能超过 " + maxSizeBytes / 1024 / 1024 + "MB");
        }
        String extension = extractExtension(file.getOriginalFilename());
        if (!allowedExtensions.contains(extension)) {
            throw new FileStorageException("不支持的文件类型");
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new FileStorageException("文件名不能为空");
        }
        String sanitized = Path.of(originalFilename).getFileName().toString();
        int dotIndex = sanitized.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == sanitized.length() - 1) {
            throw new FileStorageException("文件必须包含扩展名");
        }
        return sanitized.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeRelativePath(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new FileStorageException("文件路径不能为空");
        }
        String normalized = relativePath.trim().replace('\\', '/');
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("uploads/")) {
            normalized = normalized.substring("uploads/".length());
        }
        return normalized;
    }

    private boolean isImageFile(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == name.length() - 1) {
            return false;
        }
        return IMAGE_EXTENSIONS.contains(name.substring(dotIndex + 1));
    }

    private long lastModifiedEpoch(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException exception) {
            return 0L;
        }
    }

    private StoredFile toStoredFile(Path path) {
        try {
            long size = Files.size(path);
            LocalDateTime lastModifiedAt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(Files.getLastModifiedTime(path).toMillis()),
                    ZoneId.systemDefault()
            );
            String name = path.getFileName().toString();
            return new StoredFile(name, size, lastModifiedAt, "/uploads/" + name);
        } catch (IOException exception) {
            throw new FileStorageException("读取文件信息失败", exception);
        }
    }

    public record StoredFile(String name, long size, LocalDateTime lastModifiedAt, String url) {
    }
}
