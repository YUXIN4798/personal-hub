package com.tianshi.hub.exception;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void handleMaxUploadSizeExceeded_返回中文提示() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Model model = new ConcurrentModel();

        String view = handler.handleMaxUploadSizeExceeded(model);

        assertThat(view).isEqualTo("error/400");
        assertThat(model.getAttribute("message")).isEqualTo("文件过大，请上传不超过 5MB 的文件。");
    }

    @Test
    void handleFileStorageException_返回异常中文消息() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Model model = new ConcurrentModel();

        String view = handler.handleFileStorageException(new FileStorageException("保存文件失败"), model);

        assertThat(view).isEqualTo("error/400");
        assertThat(model.getAttribute("message")).isEqualTo("保存文件失败");
    }
}
