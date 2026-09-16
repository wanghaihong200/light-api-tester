package com.wang.light.example;

import com.wang.light.api.client.ApiClient;
import com.wang.light.api.retry.ApiRetryAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.is;

/**
 * 重试器演示：/api/flaky 首次 500，重试后 200。
 * 重试规则：只重试异常/超时/5xx，业务断言失败永不重试。
 */
@SpringBootTest(classes = DemoApp.class)
public class RetryShowcaseTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ApiClient apiClient;

    @Test(retryAnalyzer = ApiRetryAnalyzer.class)
    public void flakyEndpoint_recoversAfterRetry() {
        apiClient.get("/api/flaky").execute().assertThat()
                .statusCode(200)
                .jsonPath("$.data", is("ok"));
    }
}
