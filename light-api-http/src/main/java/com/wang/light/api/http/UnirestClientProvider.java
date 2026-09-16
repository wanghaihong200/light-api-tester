package com.wang.light.api.http;

import com.wang.light.api.client.ApiClient;
import com.wang.light.api.config.LightApiConfig;
import com.wang.light.api.config.ProfileConfig;
import com.wang.light.api.spi.ApiClientProvider;

/** http 协议提供者（ServiceLoader 注册，见 META-INF/services） */
public class UnirestClientProvider implements ApiClientProvider {

    @Override
    public String protocol() {
        return "http";
    }

    @Override
    public ApiClient create(ProfileConfig profile, LightApiConfig config) {
        return new UnirestApiClient(profile);
    }
}
