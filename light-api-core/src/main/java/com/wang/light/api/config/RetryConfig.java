package com.wang.light.api.config;

/**
 * 失败重试配置。maxAttempts 为首跑之后的额外次数，0 = 关闭（默认，防止掩盖真问题）。
 * 生效条件见 {@link com.wang.light.api.retry.ApiRetryAnalyzer}。
 */
public class RetryConfig {

    private int maxAttempts = 0;

    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
}
