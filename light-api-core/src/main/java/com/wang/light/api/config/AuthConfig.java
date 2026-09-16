package com.wang.light.api.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 鉴权配置。内置 token 型（配置驱动，框架不写死任何登录接口）；
 * 其他类型由业务工程实现 AuthProvider SPI 并注册同名字的 type。
 */
public class AuthConfig {

    public static final String TYPE_TOKEN = "token";

    /** 鉴权类型：token 为内置实现；空表示该环境不需要鉴权 */
    private String type;

    /** 登录接口路径（相对 base-url） */
    private String loginPath;

    private String usernameField = "username";
    private String passwordField = "password";
    private String username;
    private String password;

    /** 从登录响应中提取 token 的 JsonPath */
    private String tokenJsonPath = "$.data.token";

    /** 业务请求携带 token 的 header 名 */
    private String tokenHeader = "Authorization";

    /** token 前缀（如 "Bearer "），拼在 token 前，可为空串 */
    private String tokenPrefix = "Bearer ";

    /** 收到 401 时自动作废缓存并重新登录重试一次 */
    @JsonProperty("relogin-on-401")
    private boolean reloginOn401 = true;

    public boolean enabled() {
        return type != null && !type.trim().isEmpty();
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLoginPath() { return loginPath; }
    public void setLoginPath(String loginPath) { this.loginPath = loginPath; }

    public String getUsernameField() { return usernameField; }
    public void setUsernameField(String usernameField) { this.usernameField = usernameField; }

    public String getPasswordField() { return passwordField; }
    public void setPasswordField(String passwordField) { this.passwordField = passwordField; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTokenJsonPath() { return tokenJsonPath; }
    public void setTokenJsonPath(String tokenJsonPath) { this.tokenJsonPath = tokenJsonPath; }

    public String getTokenHeader() { return tokenHeader; }
    public void setTokenHeader(String tokenHeader) { this.tokenHeader = tokenHeader; }

    public String getTokenPrefix() { return tokenPrefix; }
    public void setTokenPrefix(String tokenPrefix) { this.tokenPrefix = tokenPrefix; }

    public boolean isReloginOn401() { return reloginOn401; }
    public void setReloginOn401(boolean reloginOn401) { this.reloginOn401 = reloginOn401; }
}
