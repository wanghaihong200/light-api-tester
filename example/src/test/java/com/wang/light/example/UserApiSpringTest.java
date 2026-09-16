package com.wang.light.example;

import com.wang.light.api.client.ApiResponse;
import com.wang.light.example.model.User;
import com.wang.light.example.service.UserApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * 标准 Spring 集成用法：@SpringBootTest + TestNG，Service Bean 注入。
 */
@SpringBootTest(classes = DemoApp.class)
public class UserApiSpringTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private UserApiService userApiService;

    @Test
    public void getUser_returnsMappedPojo() {
        User user = userApiService.getUser(1);
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("tom");
    }

    @Test
    public void listUsers_supportsFluentAssertion() {
        ApiResponse resp = userApiService.listUsers(1);
        resp.assertThat()
                .statusCode(200)
                .jsonPath("$.data[0].name", is("tom"))
                .jsonPath("$.total").isEqualTo(1);
    }

    @Test
    public void createUser_demoServiceLayer() {
        User created = userApiService.createUser("fresh");
        assertThat(created.getId()).isEqualTo(42L);
        assertThat(created.getName()).isNotEmpty();
    }
}
