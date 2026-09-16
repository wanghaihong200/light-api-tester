package com.wang.light.api.assertion;

import com.wang.light.api.client.ApiResponse;
import com.wang.light.api.util.Json;
import com.wang.light.api.util.JsonPaths;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

/**
 * ApiResponse 的流式断言入口。
 *
 * <pre>
 * resp.assertThat()
 *     .statusCode(200)
 *     .jsonPath("$.data.name").isEqualTo("tom")   // AssertJ 风格
 *     .jsonPath("$.data.id", notNullValue())      // hamcrest 匹配器风格
 *     .bodyContains("ok");
 * </pre>
 */
public class ApiAssert {

    private final ApiResponse response;

    public ApiAssert(ApiResponse response) {
        this.response = response;
    }

    public ApiAssert statusCode(int expected) {
        Assertions.assertThat(response.status())
                .as(desc("HTTP 状态码应为 " + expected))
                .isEqualTo(expected);
        return this;
    }

    public ApiAssert isSuccessful() {
        Assertions.assertThat(response.status())
                .as(desc("应返回 2xx"))
                .isBetween(200, 299);
        return this;
    }

    public JsonPathAssert jsonPath(String path) {
        return new JsonPathAssert(response, path);
    }

    public ApiAssert jsonPath(String path, Object expected) {
        jsonPath(path).isEqualTo(expected);
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ApiAssert jsonPath(String path, Matcher<?> matcher) {
        Object actual = JsonPaths.read(response, path);
        MatcherAssert.assertThat(desc(), actual, (Matcher) matcher);
        return this;
    }

    public ApiAssert bodyContains(String fragment) {
        Assertions.assertThat(response.bodyAsString())
                .as(desc("响应体应包含: " + fragment))
                .contains(fragment);
        return this;
    }

    public ApiResponse response() {
        return response;
    }

    private String desc() {
        return response.request() + " | status=" + response.status()
                + " | body=" + Json.snippet(response.bodyAsString());
    }

    private String desc(String intent) {
        return intent + " | " + desc();
    }
}
