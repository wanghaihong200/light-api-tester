package com.wang.light.api.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.wang.light.api.exception.LightApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 配置加载：classpath 下的 light-api/config.yaml，支持三类环境覆盖（优先级从高到低）：
 * <ol>
 *   <li>系统属性：-Denv=xxx；-Dlight-api.base-url=xxx（落到当前环境的 profile）；-Dlight-api.publish.url=xxx（顶级键）</li>
 *   <li>环境变量：LIGHT_API_ENV=xxx；LIGHT_API_BASE_URL=xxx（下划线转 kebab）</li>
 *   <li>yaml 里的 env 键；若三者皆无且 profiles 恰好只有一个，则用唯一 profile 兜底</li>
 * </ol>
 */
public final class ConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);

    public static final String CONFIG_RESOURCE = "light-api/config.yaml";
    public static final String ENV_SYS_PROP = "env";
    public static final String ENV_ENV_VAR = "LIGHT_API_ENV";
    public static final String OVERRIDE_PREFIX = "light-api.";
    private static final String ENV_VAR_PREFIX = "LIGHT_API_";

    private static final Set<String> TOP_LEVEL_KEYS =
            new HashSet<>(Arrays.asList("env", "profiles", "publish", "retry"));

    private ConfigLoader() {
    }

    public static LightApiConfig load() {
        ObjectNode root = readYaml();
        String env = resolveEnv(root);
        applyOverrides(root, env);

        LightApiConfig config;
        try {
            config = bindingMapper().treeToValue(root, LightApiConfig.class);
        } catch (IOException e) {
            throw new LightApiException("config.yaml 绑定到 LightApiConfig 失败: " + e.getMessage(), e);
        }
        if (config.getProfiles() == null || config.getProfiles().isEmpty()) {
            throw new LightApiException("config.yaml 未定义任何 profiles，至少需要一个环境配置块");
        }
        config.setEnv(env);
        log.info("light-api 配置加载完成: env={}, baseUrl={}", env, config.activeProfile().getBaseUrl());
        return config;
    }

    private static ObjectNode readYaml() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ConfigLoader.class.getClassLoader();
        }
        java.net.URL url = cl.getResource(CONFIG_RESOURCE);
        if (url == null) {
            throw new LightApiException("未找到 classpath:" + CONFIG_RESOURCE
                    + "，请在业务工程 src/test/resources/light-api/ 下提供 config.yaml");
        }
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory())
                .configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        try (InputStream in = url.openStream()) {
            JsonNode node = yamlMapper.readTree(in);
            if (node == null || !node.isObject()) {
                return new ObjectMapper().createObjectNode();
            }
            return (ObjectNode) node;
        } catch (IOException e) {
            throw new LightApiException("解析 " + CONFIG_RESOURCE + " 失败: " + e.getMessage(), e);
        }
    }

    private static String resolveEnv(ObjectNode root) {
        String fromSys = System.getProperty(ENV_SYS_PROP);
        String fromEnvVar = System.getenv(ENV_ENV_VAR);
        String env = firstNonBlank(fromSys, fromEnvVar, textOrNull(root.get("env")));
        if (env == null) {
            JsonNode profiles = root.get("profiles");
            if (profiles != null && profiles.isObject() && profiles.size() == 1) {
                Iterator<String> it = profiles.fieldNames();
                env = it.next();
                log.info("未显式指定环境，使用唯一 profile: {}", env);
            } else {
                throw new LightApiException("无法确定环境：请通过 -Denv=xxx 或环境变量 LIGHT_API_ENV 指定（或保证 yaml 中 profiles 只有一个）");
            }
        }
        root.put("env", env);
        return env;
    }

    private static void applyOverrides(ObjectNode root, String env) {
        Map<String, String> overrides = new LinkedHashMap<>();
        for (String name : System.getProperties().stringPropertyNames()) {
            if (name.startsWith(OVERRIDE_PREFIX)) {
                overrides.put(name.substring(OVERRIDE_PREFIX.length()), System.getProperty(name));
            }
        }
        for (Map.Entry<String, String> kv : System.getenv().entrySet()) {
            if (kv.getKey().startsWith(ENV_VAR_PREFIX)) {
                String key = kv.getKey().substring(ENV_VAR_PREFIX.length())
                        .toLowerCase(Locale.ROOT).replace('_', '-');
                overrides.putIfAbsent(key, kv.getValue());
            }
        }

        for (Map.Entry<String, String> ov : overrides.entrySet()) {
            String key = ov.getKey();
            String value = ov.getValue();
            String target;
            if (key.startsWith("profiles.")) {
                log.warn("忽略不支持的覆盖项 light-api.{}（profiles 结构由 yaml 管理）", key);
                continue;
            }
            String firstSeg = key.contains(".") ? key.substring(0, key.indexOf('.')) : key;
            target = TOP_LEVEL_KEYS.contains(firstSeg) ? key : "profiles." + env + "." + key;
            setByDottedPath(root, target, value);
            log.info("配置覆盖: -Dlight-api.{}={} -> {}", key, value, target);
        }
    }

    private static void setByDottedPath(ObjectNode root, String path, String value) {
        String[] segs = path.split("\\.");
        ObjectNode cur = root;
        for (int i = 0; i < segs.length - 1; i++) {
            JsonNode next = cur.get(segs[i]);
            if (next == null || !next.isObject()) {
                next = cur.putObject(segs[i]);
            }
            cur = (ObjectNode) next;
        }
        cur.put(segs[segs.length - 1], value);
    }

    private static ObjectMapper bindingMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return v.trim();
            }
        }
        return null;
    }

    private static String textOrNull(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
    }
}
