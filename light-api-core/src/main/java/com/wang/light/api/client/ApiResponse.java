package com.wang.light.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.wang.light.api.assertion.ApiAssert;
import com.wang.light.api.util.Json;
import com.wang.light.api.util.JsonPaths;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/**
 * 协议无关的统一响应封装。任何状态码都正常返回（成败由断言裁决），
 * 引擎层的传输异常才会以异常形式抛出。
 */
public class ApiResponse {

    private final ApiRequest request;
    private final int status;
    private final Map<String, String> headers;
    private final byte[] body;
    private final long durationMs;

    public ApiResponse(ApiRequest request, int status, Map<String, String> headers, byte[] body, long durationMs) {
        this.request = request;
        this.status = status;
        this.headers = headers == null ? Collections.emptyMap() : headers;
        this.body = body == null ? new byte[0] : body;
        this.durationMs = durationMs;
    }

    public int status() {
        return status;
    }

    public String header(String name) {
        return headers.get(name);
    }

    public Map<String, String> headers() {
        return Collections.unmodifiableMap(headers);
    }

    /** 响应体原文，按 UTF-8 解码 */
    public String bodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    public byte[] bodyAsBytes() {
        return body;
    }

    /** 响应体解析为 JSON 树；非 JSON 响应体会抛出带原文片段的异常 */
    public JsonNode json() {
        return Json.parse(bodyAsString());
    }

    /** 按给定的实体类型反序列化响应体 */
    public <T> T as(Class<T> type) {
        return Json.parseObject(bodyAsString(), type);
    }

    /** 从响应体按 JsonPath 取出子树并映射为 POJO（适配 {code,data} 包裹式响应） */
    public <T> T as(String jsonPath, Class<T> type) {
        return Json.convert(jsonPath(jsonPath), type);
    }

    /** 读取 JsonPath（只读场景）；断言场景请使用 {@link #assertThat()} */
    public Object jsonPath(String path) {
        return JsonPaths.read(this, path);
    }

    /** 进入流式断言入口 */
    public ApiAssert assertThat() {
        return new ApiAssert(this);
    }

    public ApiRequest request() {
        return request;
    }

    public long durationMs() {
        return durationMs;
    }

    @Override
    public String toString() {
        return request + " -> " + status + " (" + durationMs + "ms)";
    }
}
