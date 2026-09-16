package com.wang.light.api.report;

import com.wang.light.api.bootstrap.LightApiManager;
import com.wang.light.api.config.PublishConfig;
import com.wang.light.api.publish.TestSummary;
import com.wang.light.api.spi.ResultPublisher;
import com.wang.light.api.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 测试平台推送器（ADR-0005）：套件结束后把 TestSummary 以 JSON POST 到平台。
 * 用 JDK HttpURLConnection，不绑定 HTTP 引擎；失败只告警（不影响退出码）。
 */
public class HttpResultPublisher implements ResultPublisher {

    private static final Logger log = LoggerFactory.getLogger(HttpResultPublisher.class);

    @Override
    public void publish(TestSummary summary) {
        PublishConfig pc = LightApiManager.config().getPublish();
        if (pc == null || !pc.isEnabled()) {
            return;
        }
        if (pc.getUrl() == null || pc.getUrl().trim().isEmpty()) {
            log.warn("[light-api] publish.enabled=true 但未配置 publish.url，跳过推送");
            return;
        }
        try {
            byte[] payload = Json.toJson(summary).getBytes(StandardCharsets.UTF_8);
            HttpURLConnection conn = (HttpURLConnection) new URL(pc.getUrl()).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            if (pc.getToken() != null && !pc.getToken().isEmpty()) {
                conn.setRequestProperty(pc.getTokenHeader(), pc.getToken());
            }
            OutputStream out = conn.getOutputStream();
            out.write(payload);
            out.flush();
            out.close();
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                log.error("[light-api] 推送测试平台失败 status={} url={}", code, pc.getUrl());
            } else {
                log.info("[light-api] 结果已推送到测试平台: {}", pc.getUrl());
            }
        } catch (Exception e) {
            log.error("[light-api] 推送测试平台异常（不影响测试结果）: {}", e.getMessage(), e);
        }
    }
}
