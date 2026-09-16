package com.wang.light.api.config;

import com.wang.light.api.exception.LightApiException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 框架总配置，对应业务工程 {@code src/test/resources/light-api/config.yaml}。
 * env 在加载时解析（-Denv > 环境变量 LIGHT_API_ENV > yaml 的 env 键 > 唯一 profile 兜底）。
 */
public class LightApiConfig {

    /** 当前生效的环境名（profiles 的键） */
    private String env;

    private Map<String, ProfileConfig> profiles = new LinkedHashMap<>();

    private PublishConfig publish = new PublishConfig();

    private RetryConfig retry = new RetryConfig();

    public ProfileConfig activeProfile() {
        if (env == null || !profiles.containsKey(env)) {
            throw new LightApiException("环境 '" + env + "' 在 light-api/config.yaml 的 profiles 中不存在，可用环境: "
                    + profiles.keySet());
        }
        return profiles.get(env);
    }

    public String getEnv() { return env; }
    public void setEnv(String env) { this.env = env; }

    public Map<String, ProfileConfig> getProfiles() { return profiles; }
    public void setProfiles(Map<String, ProfileConfig> profiles) { this.profiles = profiles; }

    public PublishConfig getPublish() { return publish; }
    public void setPublish(PublishConfig publish) { this.publish = publish; }

    public RetryConfig getRetry() { return retry; }
    public void setRetry(RetryConfig retry) { this.retry = retry; }
}
