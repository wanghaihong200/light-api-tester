package com.wang.light.api.client;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 协议无关的请求描述对象。业务代码不直接构造它，通过 {@link ApiClient} 的 Fluent 链构建。
 */
public class ApiRequest {

    private final ApiMethod method;
    private final String url;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final Map<String, Object> query = new LinkedHashMap<>();
    private final Map<String, Object> fields = new LinkedHashMap<>();
    /** POJO 由引擎序列化为 JSON；String 视为原始 JSON 串 */
    private Object body;

    public ApiRequest(ApiMethod method, String url) {
        this.method = method;
        this.url = url;
    }

    public ApiRequest header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public ApiRequest query(String name, Object value) {
        query.put(name, value);
        return this;
    }

    public ApiRequest field(String name, Object value) {
        fields.put(name, value);
        return this;
    }

    public ApiMethod getMethod() { return method; }
    public String getUrl() { return url; }
    public Map<String, String> getHeaders() { return headers; }
    public Map<String, Object> getQuery() { return query; }
    public Map<String, Object> getFields() { return fields; }
    public Object getBody() { return body; }
    public void setBody(Object body) { this.body = body; }

    @Override
    public String toString() {
        return method + " " + url;
    }
}
