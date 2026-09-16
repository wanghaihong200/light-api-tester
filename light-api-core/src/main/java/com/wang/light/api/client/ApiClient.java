package com.wang.light.api.client;

import com.wang.light.api.config.ProfileConfig;
import com.wang.light.api.spi.AuthProvider;
import com.wang.light.api.spi.Filter;
import com.wang.light.api.spi.SpiLoader;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 协议无关的客户端基类（ADR-0002）。
 * <p>
 * execute 的模板流程：Filter 前置处理 -> 鉴权器注入凭证 -> 引擎发送（子类实现 doSend）
 * -> 401 且配置了 relogin-on-401 时作废凭证重发一次 -> Filter 后置处理。
 * <p>
 * 实现要求：实例可创建多个（多系统/多账号并存），内部不得有跨实例共享的可变状态。
 */
public abstract class ApiClient {

    /** 最近一次响应的状态码（线程级），供 ApiRetryAnalyzer 判断是否因 5xx 重试 */
    private static final ThreadLocal<Integer> LAST_STATUS = ThreadLocal.withInitial(() -> -1);

    protected final ProfileConfig profile;
    private final CopyOnWriteArrayList<Filter> filters;
    private volatile AuthProvider authProvider;

    protected ApiClient(ProfileConfig profile) {
        this.profile = profile;
        this.filters = new CopyOnWriteArrayList<>();
        this.filters.addAll(SpiLoader.loadAllOrEmpty(Filter.class));
    }

    public RequestSpec get(String path) { return spec(ApiMethod.GET, path); }

    public RequestSpec post(String path) { return spec(ApiMethod.POST, path); }

    public RequestSpec put(String path) { return spec(ApiMethod.PUT, path); }

    public RequestSpec patch(String path) { return spec(ApiMethod.PATCH, path); }

    public RequestSpec delete(String path) { return spec(ApiMethod.DELETE, path); }

    public RequestSpec spec(ApiMethod method, String path) {
        return new RequestSpec(this, new ApiRequest(method, resolveUrl(path)));
    }

    /** 挂载本客户端私有的过滤器（业务代码可用，例如压测打标） */
    public ApiClient addFilter(Filter filter) {
        filters.add(filter);
        return this;
    }

    protected void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    public AuthProvider authProvider() {
        return authProvider;
    }

    public ProfileConfig profile() {
        return profile;
    }

    /** 相对路径自动拼 baseUrl，绝对 http(s) URL 原样使用 */
    protected String resolveUrl(String path) {
        String lower = path == null ? "" : path.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return path;
        }
        String base = profile.getBaseUrl() == null ? "" : profile.getBaseUrl();
        if (!base.isEmpty() && base.endsWith("/") && path.startsWith("/")) {
            return base.substring(0, base.length() - 1) + path;
        }
        return base + path;
    }

    public final ApiResponse execute(ApiRequest request) {
        ApiRequest req = request;
        for (Filter f : filters) {
            req = f.beforeRequest(req);
        }

        ApiResponse resp = null;
        if (authProvider != null) {
            authProvider.apply(req);
            resp = doSend(req);
            boolean relogin = profile.getAuth() != null && profile.getAuth().isReloginOn401();
            if (resp.status() == 401 && relogin) {
                authProvider.invalidate();
                authProvider.apply(req);
                resp = doSend(req);
            }
        } else {
            resp = doSend(req);
        }

        for (int i = filters.size() - 1; i >= 0; i--) {
            resp = filters.get(i).afterResponse(resp);
        }
        LAST_STATUS.set(resp.status());
        return resp;
    }

    /** 协议引擎的真正发送；实现负责序列化 body/fields、日志与耗时统计 */
    protected abstract ApiResponse doSend(ApiRequest request);

    /** 最近一次（当前线程）响应的状态码；无请求记录时为 -1 */
    public static int lastStatus() {
        return LAST_STATUS.get();
    }
}
