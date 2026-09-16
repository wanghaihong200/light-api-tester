package com.wang.light.api.util;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.wang.light.api.client.ApiResponse;
import com.wang.light.api.exception.LightApiException;

import java.io.IOException;

/**
 * JsonPath 读取入口：Jackson 先把响应体解析成对象图，jayway 只负责导航。
 * 路径不存在时返回 null（SUPPRESS_EXCEPTION）。
 */
public final class JsonPaths {

    private static final Configuration CONFIG = Configuration.builder()
            .options(Option.SUPPRESS_EXCEPTIONS)
            .build();

    private JsonPaths() {
    }

    public static Object read(ApiResponse response, String path) {
        String body = response.bodyAsString();
        if (body == null || body.trim().isEmpty()) {
            throw new LightApiException("响应体为空，JsonPath 无法读取: " + path + "（" + response.request() + "）");
        }
        try {
            Object root = Json.MAPPER.readValue(body, Object.class);
            DocumentContext ctx = JsonPath.using(CONFIG).parse(root);
            return ctx.read(path);
        } catch (IOException e) {
            throw new LightApiException("响应体不是合法 JSON，JsonPath 无法读取: " + path
                    + " | body=" + Json.snippet(body), e);
        } catch (Exception e) {
            throw new LightApiException("JsonPath 读取失败: " + path
                    + " | body=" + Json.snippet(body) + " | 原因: " + e.getMessage(), e);
        }
    }

    public static String readAsString(ApiResponse response, String path) {
        Object value = read(response, path);
        return value == null ? null : String.valueOf(value);
    }
}
