package com.wang.light.api.spring;

import com.wang.light.api.bootstrap.LightApiManager;
import com.wang.light.api.client.ApiClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 框架的 Spring Boot 自动配置：注册 Spring 配置源并暴露 ApiClient Bean。
 */
@Configuration
public class LightApiAutoConfiguration {

    @Bean
    public LightApiSpringRegistrar lightApiSpringRegistrar(org.springframework.core.env.Environment env) {
        return new LightApiSpringRegistrar(env);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiClient apiClient(LightApiSpringRegistrar registrar) {
        return LightApiManager.defaultClient();
    }
}
