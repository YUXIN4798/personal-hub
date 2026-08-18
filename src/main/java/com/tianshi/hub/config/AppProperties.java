package com.tianshi.hub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String uploadDir = "./uploads";
    private final Upload upload = new Upload();
    private final Security security = new Security();

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public Upload getUpload() {
        return upload;
    }

    public Security getSecurity() {
        return security;
    }

    public static class Upload {

        private DataSize maxSize = DataSize.ofMegabytes(5);

        public DataSize getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(DataSize maxSize) {
            this.maxSize = maxSize;
        }
    }

    public static class Security {

        private java.util.List<String> trustedProxies = java.util.List.of();

        public java.util.List<String> getTrustedProxies() {
            return trustedProxies;
        }

        public void setTrustedProxies(java.util.List<String> trustedProxies) {
            this.trustedProxies = trustedProxies == null ? java.util.List.of() : trustedProxies;
        }
    }
}
