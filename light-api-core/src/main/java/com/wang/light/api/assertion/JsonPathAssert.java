package com.wang.light.api.assertion;

import com.wang.light.api.client.ApiResponse;
import com.wang.light.api.util.Json;
import com.wang.light.api.util.JsonPaths;
import org.assertj.core.api.Assertions;

/**
 * 单个 JsonPath 的断言视图。路径不存在时读到 null（SUPPRESS_EXCEPTION）。
 */
public class JsonPathAssert {

    private final ApiResponse response;
    private final String path;

    JsonPathAssert(ApiResponse response, String path) {
        this.response = response;
        this.path = path;
    }

    public JsonPathAssert isEqualTo(Object expected) {
        Assertions.assertThat(actual())
                .as(desc("JsonPath " + path + " 应等于 " + expected))
                .isEqualTo(expected);
        return this;
    }

    public JsonPathAssert isNotNull() {
        Assertions.assertThat(actual())
                .as(desc("JsonPath " + path + " 应非 null"))
                .isNotNull();
        return this;
    }

    public JsonPathAssert isNull() {
        Assertions.assertThat(actual())
                .as(desc("JsonPath " + path + " 应为 null"))
                .isNull();
        return this;
    }

    public JsonPathAssert contains(String fragment) {
        Assertions.assertThat(String.valueOf(actual()))
                .as(desc("JsonPath " + path + " 应包含 " + fragment))
                .contains(fragment);
        return this;
    }

    /** 取出实际值，结束断言链（复杂断言转 AssertJ 原生） */
    public Object value() {
        return actual();
    }

    private Object actual() {
        return JsonPaths.read(response, path);
    }

    private String desc(String intent) {
        return intent + " | " + response.request()
                + " | body=" + Json.snippet(response.bodyAsString());
    }
}
