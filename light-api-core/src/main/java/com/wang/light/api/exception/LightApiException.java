package com.wang.light.api.exception;

/**
 * 框架统一运行时异常。消息必须可直接指导排查（缺哪个配置、哪个文件、哪个 SPI）。
 */
public class LightApiException extends RuntimeException {

    public LightApiException(String message) {
        super(message);
    }

    public LightApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
