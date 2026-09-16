package com.wang.light.api.bootstrap;

import com.wang.light.api.config.LightApiConfig;
import com.wang.light.api.publish.TestCaseResult;
import com.wang.light.api.publish.TestSummary;
import com.wang.light.api.spi.ResultPublisher;
import com.wang.light.api.spi.SpiLoader;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ISuiteListener;
import org.testng.ITestResult;
import org.testng.IResultMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * 框架总监听器：套件开始时初始化框架，套件结束后汇总结果并推送给测试平台。
 * 通过 META-INF/services/org.testng.ITestNGListener 自动注册（TestNG 7 自动发现机制）。
 */
public class LightApiListener implements ISuiteListener {

    private static final Logger log = LoggerFactory.getLogger(LightApiListener.class);

    @Override
    public void onStart(ISuite suite) {
        try {
            LightApiManager.ensureInit();
            log.info("[light-api] 已初始化，env={}", LightApiManager.config().getEnv());
        } catch (Throwable t) {
            // Spring 模式下配置源要等上下文刷新才注册，此时允许延迟初始化；
            // 非 Spring 场景若配置有问题，用例首次使用框架时仍会立即失败（fail-fast 不变）。
            log.info("[light-api] 套件启动时暂不初始化（可能由 Spring 上下文稍后完成）: {}", t.getMessage());
        }
    }

    @Override
    public void onFinish(ISuite suite) {
        LightApiConfig cfg;
        try {
            cfg = LightApiManager.config();
        } catch (Throwable t) {
            log.error("[light-api] onFinish 时配置不可用，跳过推送", t);
            return;
        }
        TestSummary summary = buildSummary(suite, cfg);
        log.info("[light-api] 执行完成: total={}, passed={}, failed={}, skipped={}, 耗时={}ms",
                summary.getTotal(), summary.getPassed(), summary.getFailed(), summary.getSkipped(),
                summary.getFinishedAt() - summary.getStartedAt());
        if (cfg.getPublish() != null && cfg.getPublish().isEnabled()) {
            for (ResultPublisher p : SpiLoader.loadAllOrEmpty(ResultPublisher.class)) {
                try {
                    p.publish(summary);
                } catch (Throwable t) {
                    log.error("[light-api] 结果推送失败（不影响测试结果）: {}", t.getMessage(), t);
                }
            }
        }
    }

    private TestSummary buildSummary(ISuite suite, LightApiConfig cfg) {
        TestSummary s = new TestSummary();
        s.setEnv(cfg.getEnv());
        s.setSuiteName(suite.getName());
        long start = Long.MAX_VALUE;
        long end = 0;
        for (ISuiteResult r : suite.getResults().values()) {
            ITestContext ctx = r.getTestContext();
            start = Math.min(start, ctx.getStartDate().getTime());
            end = Math.max(end, ctx.getEndDate().getTime());
            int n = collect(ctx.getPassedTests(), TestCaseResult.STATUS_PASSED, s);
            s.setPassed(s.getPassed() + n);
            n = collect(ctx.getFailedTests(), TestCaseResult.STATUS_FAILED, s);
            s.setFailed(s.getFailed() + n);
            n = collect(ctx.getSkippedTests(), TestCaseResult.STATUS_SKIPPED, s);
            s.setSkipped(s.getSkipped() + n);
        }
        if (start == Long.MAX_VALUE) {
            start = end = System.currentTimeMillis();
        }
        s.setStartedAt(start);
        s.setFinishedAt(end);
        s.setTotal(s.getPassed() + s.getFailed() + s.getSkipped());
        return s;
    }

    private int collect(IResultMap map, String status, TestSummary s) {
        int count = 0;
        for (ITestResult tr : map.getAllResults()) {
            String msg = tr.getThrowable() == null ? null : tr.getThrowable().getMessage();
            s.getCases().add(new TestCaseResult(
                    tr.getName(),
                    tr.getTestClass() == null ? null : tr.getTestClass().getRealClass().getName(),
                    status,
                    tr.getEndMillis() - tr.getStartMillis(),
                    msg));
            count++;
        }
        return count;
    }
}
