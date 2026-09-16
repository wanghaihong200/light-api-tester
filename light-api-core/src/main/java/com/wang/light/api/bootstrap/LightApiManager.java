package com.wang.light.api.bootstrap;

import com.wang.light.api.client.ApiClient;
import com.wang.light.api.config.ConfigLoader;
import com.wang.light.api.config.ConfigSource;
import com.wang.light.api.config.LightApiConfig;
import com.wang.light.api.config.ProfileConfig;
import com.wang.light.api.exception.LightApiException;
import com.wang.light.api.spi.ApiClientProvider;
import com.wang.light.api.spi.SpiLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 框架引导单例：加载配置、装配默认客户端与内置过滤器。
 */
public final class LightApiManager {

    private static volatile LightApiManager instance;

    /** Spring Boot starter 注册的配置源，优先于内置 yaml（见 ConfigSource） */
    private static volatile ConfigSource externalConfigSource;

    /** Spring 环境下由 starter 调用；须在首次 ensureInit 之前注册 */
    public static void registerConfigSource(ConfigSource source) {
        externalConfigSource = source;
    }

    private final LightApiConfig config;
    private final ApiClient defaultClient;

    private LightApiManager(LightApiConfig config) {
        this.config = config;
        this.defaultClient = newClientFor(config, config.getEnv());
    }

    public static void ensureInit() {
        if (instance == null) {
            synchronized (LightApiManager.class) {
                if (instance == null) {
                    ConfigSource source = externalConfigSource;
                    LightApiConfig cfg = source != null ? source.load() : ConfigLoader.load();
                    instance = new LightApiManager(cfg);
                }
            }
        }
    }

    public static LightApiConfig config() {
        ensureInit();
        return instance.config;
    }

    public static ProfileConfig activeProfile() {
        return config().activeProfile();
    }

    public static ApiClient defaultClient() {
        ensureInit();
        return instance.defaultClient;
    }

    /** 按环境名创建独立客户端（多套系统/多账号并存场景） */
    public static ApiClient createClient(String envName) {
        LightApiManager m = init();
        return newClientFor(m.config, envName == null ? m.config.getEnv() : envName);
    }

    /** 测试场景下重建配置（业务工程极少用） */
    public static synchronized void reset() {
        instance = null;
    }

    private static LightApiManager init() {
        ensureInit();
        return instance;
    }

    private static ApiClient newClientFor(LightApiConfig cfg, String envName) {
        ProfileConfig profile = cfg.getProfiles().get(envName);
        if (profile == null) {
            throw new LightApiException("环境 '" + envName + "' 不存在，可用环境: " + cfg.getProfiles().keySet());
        }
        String protocol = profile.getProtocol() == null ? "http" : profile.getProtocol();
        for (ApiClientProvider p : SpiLoader.loadAll(ApiClientProvider.class)) {
            if (protocol.equalsIgnoreCase(p.protocol())) {
                return p.create(profile, cfg);
            }
        }
        throw new LightApiException("classpath 上没有 protocol=" + protocol + " 的 ApiClientProvider 实现（请引入 light-api-http 或 light-api-starter）");
    }
}
