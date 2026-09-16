package com.wang.light.api.config;

/**
 * 测试平台推送配置（ADR-0005：框架只推送，渲染归测试平台）。
 */
public class PublishConfig {

    /** 默认关闭；本地跑不推送，CI 里用 -Dlight-api.publish.enabled=true 打开 */
    private boolean enabled = false;

    /** 测试平台数据接收接口完整 URL */
    private String url;

    /** 可选的鉴权凭证值 */
    private String token;

    /** 鉴权 header 名 */
    private String tokenHeader = "X-Publish-Token";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenHeader() { return tokenHeader; }
    public void setTokenHeader(String tokenHeader) { this.tokenHeader = tokenHeader; }
}
