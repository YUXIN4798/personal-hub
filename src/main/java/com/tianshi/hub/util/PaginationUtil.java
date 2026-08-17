package com.tianshi.hub.util;

public final class PaginationUtil {

    private PaginationUtil() {
    }

    public static PageBounds clamp(int page, int size, int defaultSize, int maxSize) {
        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? Math.min(size, maxSize) : defaultSize;
        return new PageBounds(safePage, safeSize);
    }

    public record PageBounds(int page, int size) {
    }
}
