package com.wang.light.api.spi;

import com.wang.light.api.client.ApiRequest;

/**
 * 鉴权器 SPI。实现通过 META-INF/services 注册，实现类的 {@link #type()}
 * 对应配置 {@code auth.type}，框架按类型名路由。
 * <p>
 * 内置 "token" 实现由 light-api-http 提供（配置驱动，不写死登录接口）。
 */
public interface AuthProvider {

    /** 鉴权类型名，对应配置 auth.type；内置 token 型返回 "token" */
    String type();

    /** 为请求注入鉴权凭证（必要时懒登录并缓存） */
    void apply(ApiRequest request);

    /** 作废缓存的凭证（收到 401 时由框架调用，下次 apply 会重新获取） */
    void invalidate();
}
