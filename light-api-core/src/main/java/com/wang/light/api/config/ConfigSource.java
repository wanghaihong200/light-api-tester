package com.wang.light.api.config;

/**
 * 配置源桥接点。Spring Boot starter 会注册一个从 Spring Environment
 * （application.yml + Spring profile）读取 light-api.* 配置的实现并优先使用；
 * 无 Spring 时回退到内置的 classpath:light-api/config.yaml。
 */
public interface ConfigSource {

    LightApiConfig load();
}
