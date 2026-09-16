package com.wang.light.api.config;

/**
 * 单个命名数据源配置（light-api-db 使用）。
 */
public class DataSourceConfig {

    private String url;

    private String username;

    private String password;

    /** 可选；不填时由 url 前缀自动推断（目前支持 jdbc:mysql） */
    private String driverClassName;

    private int maximumPoolSize = 5;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDriverClassName() { return driverClassName; }
    public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }

    public int getMaximumPoolSize() { return maximumPoolSize; }
    public void setMaximumPoolSize(int maximumPoolSize) { this.maximumPoolSize = maximumPoolSize; }
}
