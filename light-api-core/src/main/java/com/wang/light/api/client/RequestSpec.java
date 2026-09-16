package com.wang.light.api.client;

import java.util.Map;

/**
 * Fluent 请求构建器，业务脚本日常接触最多的入口之一：
 * <pre>{@code
 * ApiResponse resp = Api.client()
 *     .post("/api/users")
 *     .header("X-Req-Id", id)
 *     .query("page", 1)
 *     .body(createReq)      // POJO -> JSON；传 String 则视为原始 JSON 串
 *     .execute();
 * }</pre>
 */
public final class RequestSpec {

    private final ApiClient client;
    private final ApiRequest request;

    RequestSpec(ApiClient client, ApiRequest request) {
        this.client = client;
        this.request = request;
    }

    public RequestSpec header(String name, String value) {
        request.header(name, value);
        return this;
    }

    public RequestSpec headers(Map<String, String> headers) {
        if (headers != null) {
            headers.forEach(request::header);
        }
        return this;
    }

    public RequestSpec query(String name, Object value) {
        request.query(name, value);
        return this;
    }

    public RequestSpec queries(Map<String, ?> params) {
        if (params != null) {
            params.forEach(request::query);
        }
        return this;
    }

    /** POJO 由引擎按 JSON 序列化；传 String 视为原始 JSON 串 */
    public RequestSpec body(Object body) {
        request.setBody(body);
        return this;
    }

    /** 表单/分片字段；值为 {@link java.io.File} 或 byte[] 时引擎按文件处理 */
    public RequestSpec field(String name, Object value) {
        request.field(name, value);
        return this;
    }

    public ApiResponse execute() {
        return client.execute(request);
    }
}
