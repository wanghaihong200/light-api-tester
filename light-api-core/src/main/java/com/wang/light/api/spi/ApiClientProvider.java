package com.wang.light.api.spi;

import com.wang.light.api.client.ApiClient;
import com.wang.light.api.config.LightApiConfig;
import com.wang.light.api.config.ProfileConfig;

/**
 * 协议实现提供者 SPI（ADR-0002 的扩展点）。实现通过 META-INF/services 注册，
 * 按 {@link #protocol()} 与配置 {@code profile.protocol}（默认 http）路由。
 * light-api-http 提供 "http" 实现；未来 RPC 提供自己的实现，业务用例零改动。
 */
public interface ApiClientProvider {

    /** 协议名，对应配置 profile.protocol */
    String protocol();

    /**
     * 创建客户端实例。实现负责：
     * 初始化引擎（超时等）、按 auth.type 解析 AuthProvider 并注入客户端。
     */
    ApiClient create(ProfileConfig profile, LightApiConfig config);
}
