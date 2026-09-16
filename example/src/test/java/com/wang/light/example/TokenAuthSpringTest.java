package com.wang.light.example;

import com.wang.light.api.client.ApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.is;

/**
 * 鉴权器演示：框架自动登录、注入 token；401 自动重登由配置 relogin-on-401 控制。
 */
@SpringBootTest(classes = DemoApp.class)
public class TokenAuthSpringTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ApiClient apiClient;

    @Test
    public void protectedEndpoint_usesAutoLoginToken() {
        apiClient.get("/api/me").execute().assertThat()
                .statusCode(200)
                .jsonPath("$.data.user", is("demo"));
    }
}
