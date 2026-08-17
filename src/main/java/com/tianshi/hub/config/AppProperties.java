package com.tianshi.hub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String uploadDir = "./uploads";
    private final Upload upload = new Upload();

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public Upload getUpload() {
        return upload;
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
}
