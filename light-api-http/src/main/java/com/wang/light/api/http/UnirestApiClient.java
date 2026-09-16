package com.wang.light.api.http;

import com.wang.light.api.client.ApiClient;
import com.wang.light.api.client.ApiMethod;
import com.wang.light.api.client.ApiRequest;
import com.wang.light.api.client.ApiResponse;
import com.wang.light.api.config.AuthConfig;
import com.wang.light.api.config.ProfileConfig;
import com.wang.light.api.exception.LightApiException;
import com.wang.light.api.spi.AuthProvider;
import com.wang.light.api.spi.SpiLoader;
import com.wang.light.api.util.Json;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.MultipartBody;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.UnirestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Unirest 引擎实现（ADR-0003）。每个客户端持有独立 UnirestInstance（多实例互不干扰）。
 */
public class UnirestApiClient extends ApiClient {

    private static final Logger log = LoggerFactory.getLogger(UnirestApiClient.class);

    private final UnirestInstance unirest;

    public UnirestApiClient(ProfileConfig profile) {
        super(profile);
        this.unirest = Unirest.spawnInstance();
        this.unirest.config()
                .connectTimeout(profile.getConnectTimeoutSeconds() * 1000)
                .socketTimeout(profile.getReadTimeoutSeconds() * 1000)
                .automaticRetries(false);
        resolveAuthProvider();
    }

    /** 供 TokenAuthProvider 登录复用同一引擎 */
    public ApiResponse send(ApiRequest request) {
        return doSend(request);
    }

    @Override
    protected ApiResponse doSend(ApiRequest request) {
        long start = System.currentTimeMillis();
        if (profile.isLogBody()) {
            log.info("[light-api] >> {} {}", request.getMethod(), request.getUrl());
        }
        try {
            kong.unirest.HttpRequest<?> executable = buildExecutable(request);
            HttpResponse<byte[]> resp = executable.asBytes();
            return toApiResponse(request, resp, System.currentTimeMillis() - start);
        } catch (LightApiException e) {
            throw e;
        } catch (UnirestException e) {
            throw new LightApiException("请求发送失败（网络/超时）: " + request + " | " + e.getMessage(), e);
        }
    }

    /**
     * unirest 3.x 的 body()/field() 返回新节点并承载请求体状态，
     * 必须在最终节点上执行请求——在 HttpRequestWithBody 容器上执行会丢 body（3.14.5 实测）。
     */
    private kong.unirest.HttpRequest<?> buildExecutable(ApiRequest request) {
        kong.unirest.HttpRequest<?> req = createByMethod(request);
        applyHeadersAndQuery(req, request);

        if (!request.getFields().isEmpty()) {
            HttpRequestWithBody wb = (HttpRequestWithBody) req;
            MultipartBody mp = null;
            for (Map.Entry<String, Object> f : request.getFields().entrySet()) {
                String name = f.getKey();
                Object v = f.getValue();
                if (mp == null) {
                    mp = v instanceof File ? wb.field(name, (File) v) : wb.field(name, String.valueOf(v));
                } else {
                    mp = v instanceof File ? mp.field(name, (File) v) : mp.field(name, String.valueOf(v));
                }
            }
            return mp;
        }
        Object body = request.getBody();
        if (body != null) {
            HttpRequestWithBody wb = (HttpRequestWithBody) req;
            return wb.body(body instanceof String ? (String) body : Json.toJson(body));
        }
        return req;
    }

    private kong.unirest.HttpRequest<?> createByMethod(ApiRequest request) {
        switch (request.getMethod()) {
            case GET:
                return unirest.get(request.getUrl());
            case POST:
                return unirest.post(request.getUrl());
            case PUT:
                return unirest.put(request.getUrl());
            case PATCH:
                return unirest.patch(request.getUrl());
            case DELETE:
                return unirest.delete(request.getUrl());
            default:
                throw new LightApiException("不支持的 HTTP 方法: " + request.getMethod());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void applyHeadersAndQuery(kong.unirest.HttpRequest req, ApiRequest request) {
        for (Map.Entry<String, String> h : request.getHeaders().entrySet()) {
            req.header(h.getKey(), h.getValue());
        }
        for (Map.Entry<String, Object> q : request.getQuery().entrySet()) {
            req.queryString(q.getKey(), q.getValue());
        }
    }

    private ApiResponse toApiResponse(ApiRequest request, HttpResponse<byte[]> resp, long durationMs) {
        Map<String, String> headers = new java.util.LinkedHashMap<>();
        resp.getHeaders().all().forEach(h -> headers.put(h.getName(), h.getValue()));
        byte[] body = resp.getBody() == null ? new byte[0] : resp.getBody();
        if (profile.isLogBody()) {
            String text = new String(body, java.nio.charset.StandardCharsets.UTF_8);
            log.info("[light-api] << {} ({}ms) status={} body={}",
                    request.getMethod() + " " + request.getUrl(), durationMs, resp.getStatus(),
                    profile.getMaxLogBodyLength() >= 0 && text.length() > profile.getMaxLogBodyLength()
                            ? text.substring(0, profile.getMaxLogBodyLength()) + "...(截断)"
                            : text);
        }
        return new ApiResponse(request, resp.getStatus(), headers, body, durationMs);
    }

    private void resolveAuthProvider() {
        AuthConfig auth = profile.getAuth();
        if (auth == null || !auth.enabled()) {
            return;
        }
        // 内置 token 型由引擎直接装配（依赖客户端实例发登录请求）
        if (AuthConfig.TYPE_TOKEN.equalsIgnoreCase(auth.getType())) {
            setAuthProvider(new TokenAuthProvider(this, profile));
            return;
        }
        // 其他类型走 SPI（业务自定义实现须有无参构造器并通过 META-INF/services 注册）
        for (AuthProvider p : SpiLoader.loadAll(AuthProvider.class)) {
            if (auth.getType().equalsIgnoreCase(p.type())) {
                setAuthProvider(p);
                return;
            }
        }
        throw new LightApiException("没有 type=" + auth.getType() + " 的 AuthProvider 实现（自定义实现需注册 META-INF/services 并提供无参构造器）");
    }

    public ProfileConfig profile() {
        return profile;
    }
}
