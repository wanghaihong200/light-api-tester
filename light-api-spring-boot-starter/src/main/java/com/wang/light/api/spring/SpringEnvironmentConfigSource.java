package com.wang.light.api.spring;

import com.wang.light.api.config.ConfigSource;
import com.wang.light.api.config.LightApiConfig;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

/**
 * 从 Spring Environment 读取 light-api.* 配置（application.yml + Spring profile 体系），
 * Boot Binder 松散绑定自动处理 kebab-case。
 */
public class SpringEnvironmentConfigSource implements ConfigSource {

    private final Environment environment;

    public SpringEnvironmentConfigSource(Environment environment) {
        this.environment = environment;
    }

    @Override
    public LightApiConfig load() {
        BindResult<LightApiConfig> bound = Binder.get(environment)
                .bind("light-api", Bindable.of(LightApiConfig.class));
        if (!bound.isBound()) {
            throw new IllegalStateException(
                    "Spring Environment 中没有 light-api 配置前缀，请在 application.yml 配置 light-api.*");
        }
        LightApiConfig cfg = bound.get();
        String env = environment.getProperty("light-api.env");
        if (env == null || env.trim().isEmpty()) {
            String[] active = environment.getActiveProfiles();
            if (active.length > 0) {
                env = active[0];
            }
        }
        if (env != null && !env.trim().isEmpty()) {
            cfg.setEnv(env.trim());
        }
        return cfg;
    }
}
