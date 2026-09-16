package com.wang.light.api.retry;

import com.wang.light.api.bootstrap.LightApiManager;
import com.wang.light.api.client.ApiClient;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * 内置失败重试器。默认关闭（max-attempts=0）。
 * <p>
 * 重试规则：
 * <ul>
 *   <li>异常/超时（非 AssertionError）—— 重试；</li>
 *   <li>AssertionError 且最近一次响应状态码 &gt;= 500 —— 重试（环境问题，不是业务断言失败）；</li>
 *   <li>其余 AssertionError（业务断言失败）—— 永不重试。</li>
 * </ul>
 * 用法：@Test(retryAnalyzer = ApiRetryAnalyzer.class)
 */
public class ApiRetryAnalyzer implements IRetryAnalyzer {

    private int attempts = 0;

    @Override
    public boolean retry(ITestResult result) {
        int max = 0;
        try {
            max = LightApiManager.config().getRetry().getMaxAttempts();
        } catch (Throwable ignore) {
            // 框架未初始化时按关闭处理
        }
        if (max <= 0 || attempts >= max) {
            return false;
        }
        attempts++;
        Throwable t = result.getThrowable();
        if (t == null) {
            return false;
        }
        if (t instanceof AssertionError) {
            return ApiClient.lastStatus() >= 500;
        }
        return true;
    }
}
