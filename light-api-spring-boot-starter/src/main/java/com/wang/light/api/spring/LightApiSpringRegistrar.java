package com.wang.light.api.spring;

import com.wang.light.api.bootstrap.LightApiManager;

/**
 * 在 Spring 上下文刷新时把 Spring Environment 桥接给框架，
 * 并提前完成框架初始化（保证 ApiClient Bean 可用）。
 */
public class LightApiSpringRegistrar {

    public LightApiSpringRegistrar(org.springframework.core.env.Environment env) {
        LightApiManager.registerConfigSource(new SpringEnvironmentConfigSource(env));
        LightApiManager.ensureInit();
    }
}
