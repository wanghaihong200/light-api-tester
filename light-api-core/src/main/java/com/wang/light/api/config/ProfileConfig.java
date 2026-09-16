package com.wang.light.api.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 单个环境的配置块。键名采用 kebab-case（base-url、read-timeout-seconds...）。
 */
public class ProfileConfig {

    /** 协议实现选择，http 为内置默认；未来 rpc 扩展在此落地（ADR-0002） */
    private String protocol = "http";

    private String baseUrl;

    private int connectTimeoutSeconds = 10;

    private int readTimeoutSeconds = 30;

    /** 是否打印请求/响应 body（ADR-0004：不做脱敏，内网可读性优先） */
    private boolean logBody = true;

    /** 日志中 body 的最大长度，超出截断 */
    private int maxLogBodyLength = 4096;

    private AuthConfig auth;

    private Map<String, DataSourceConfig> dataSources = new LinkedHashMap<>();

    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public int getConnectTimeoutSeconds() { return connectTimeoutSeconds; }
    public void setConnectTimeoutSeconds(int connectTimeoutSeconds) { this.connectTimeoutSeconds = connectTimeoutSeconds; }

    public int getReadTimeoutSeconds() { return readTimeoutSeconds; }
    public void setReadTimeoutSeconds(int readTimeoutSeconds) { this.readTimeoutSeconds = readTimeoutSeconds; }

    public boolean isLogBody() { return logBody; }
    public void setLogBody(boolean logBody) { this.logBody = logBody; }

    public int getMaxLogBodyLength() { return maxLogBodyLength; }
    public void setMaxLogBodyLength(int maxLogBodyLength) { this.maxLogBodyLength = maxLogBodyLength; }

    public AuthConfig getAuth() { return auth; }
    public void setAuth(AuthConfig auth) { this.auth = auth; }

    public Map<String, DataSourceConfig> getDataSources() { return dataSources; }
    public void setDataSources(Map<String, DataSourceConfig> dataSources) { this.dataSources = dataSources; }
}
