package com.wang.light.example;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * 套件级测试基建：启动 WireMock（固定端口 18089）与 Testcontainers MySQL，
 * 并通过系统属性覆盖 light-api.* 配置（Spring Environment 会优先读取系统属性）。
 * 无 Docker 时 DB 用例自动跳过。
 */
public class DemoSuiteListener implements org.testng.ISuiteListener {

    private static final Logger log = LoggerFactory.getLogger(DemoSuiteListener.class);
    private static final int WIREMOCK_PORT = 18089;

    private static WireMockServer wiremock;
    private static MySQLContainer<?> mysql;
    private static volatile boolean dbAvailable = false;

    public static boolean isDbAvailable() {
        return dbAvailable;
    }

    @Override
    public void onStart(org.testng.ISuite suite) {
        startWireMock();
        startMysql();
    }

    @Override
    public void onFinish(org.testng.ISuite suite) {
        if (wiremock != null) {
            wiremock.stop();
        }
        if (mysql != null) {
            mysql.stop();
        }
    }

    private void startWireMock() {
        wiremock = new WireMockServer(WireMockConfiguration.options().port(WIREMOCK_PORT));
        wiremock.start();
        stubAll();
        log.info("[demo] WireMock 已启动: http://localhost:{}", WIREMOCK_PORT);
    }

    private void startMysql() {
        // 新版 Docker Desktop(引擎 29.x) 与 docker-java 的 API 协商返回 400，钉住版本绕过
        System.setProperty("api.version", "1.44");
        try {
            mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.36"));
            mysql.start();
            System.setProperty("light-api.profiles.mock.data-sources.main.url", mysql.getJdbcUrl());
            System.setProperty("light-api.profiles.mock.data-sources.main.username", mysql.getUsername());
            System.setProperty("light-api.profiles.mock.data-sources.main.password", mysql.getPassword());
            dbAvailable = true;
            log.info("[demo] MySQL 容器已启动: {}", mysql.getJdbcUrl());
        } catch (Throwable t) {
            dbAvailable = false;
            log.warn("[demo] Docker 不可用，DB 相关用例将跳过: {}", t.getMessage());
        }
    }

    private void stubAll() {
        wiremock.stubFor(post(urlEqualTo("/api/login"))
                .willReturn(okJson("{\"code\":0,\"data\":{\"token\":\"demo-token-123\"}}")));

        wiremock.stubFor(get(urlEqualTo("/api/users/1"))
                .willReturn(okJson("{\"data\":{\"id\":1,\"name\":\"tom\"}}")));

        wiremock.stubFor(get(urlEqualTo("/api/users?page=1"))
                .willReturn(okJson("{\"data\":[{\"id\":1,\"name\":\"tom\"}],\"total\":1}")));

        wiremock.stubFor(post(urlEqualTo("/api/users"))
                .willReturn(okJson("{\"data\":{\"id\":42,\"name\":\"created\"}}")));

        wiremock.stubFor(get(urlEqualTo("/api/me"))
                .withHeader("Authorization", equalTo("Bearer demo-token-123"))
                .willReturn(okJson("{\"data\":{\"user\":\"demo\"}}")));
        wiremock.stubFor(get(urlEqualTo("/api/me"))
                .atPriority(10)
                .willReturn(aResponse().withStatus(401).withBody("{\"error\":\"unauthorized\"}")));

        wiremock.stubFor(post(urlEqualTo("/api/upload"))
                .willReturn(okJson("{\"code\":0,\"msg\":\"ok\"}")));

        wiremock.stubFor(get(urlEqualTo("/api/flaky"))
                .inScenario("flaky")
                .whenScenarioStateIs(Scenario.STARTED)
                .willSetStateTo("FIXED")
                .willReturn(aResponse().withStatus(500).withBody("{\"error\":\"boom\"}")));
        wiremock.stubFor(get(urlEqualTo("/api/flaky"))
                .inScenario("flaky")
                .whenScenarioStateIs("FIXED")
                .willReturn(okJson("{\"data\":\"ok\"}")));
    }
}
