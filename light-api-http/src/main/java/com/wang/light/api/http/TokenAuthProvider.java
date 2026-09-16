package com.wang.light.api.http;

import com.wang.light.api.client.ApiMethod;
import com.wang.light.api.client.ApiRequest;
import com.wang.light.api.client.ApiResponse;
import com.wang.light.api.config.AuthConfig;
import com.wang.light.api.config.ProfileConfig;
import com.wang.light.api.exception.LightApiException;
import com.wang.light.api.spi.AuthProvider;
import com.wang.light.api.util.JsonPaths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 内置 token 型鉴权器：按配置发登录请求、提取 token 并缓存；
 * 401 时由 ApiClient 模板调用 invalidate() 后重登重发（并行下加锁防登录雪崩）。
 */
public class TokenAuthProvider implements AuthProvider {

    private final UnirestApiClient client;
    private final AuthConfig auth;
    private final String baseUrl;
    private final AtomicReference<String> token = new AtomicReference<String>();
    private final ReentrantLock loginLock = new ReentrantLock();

    public TokenAuthProvider(UnirestApiClient client, ProfileConfig profile) {
        this.client = client;
        this.auth = profile.getAuth();
        this.baseUrl = profile.getBaseUrl() == null ? "" : profile.getBaseUrl();
    }

    @Override
    public String type() {
        return "token";
    }

    @Override
    public void apply(ApiRequest request) {
        String t = token.get();
        if (t == null) {
            t = login();
        }
        String prefix = auth.getTokenPrefix() == null ? "" : auth.getTokenPrefix();
        request.header(auth.getTokenHeader(), prefix + t);
    }

    @Override
    public void invalidate() {
        token.set(null);
    }

    private String login() {
        loginLock.lock();
        try {
            String t = token.get();
            if (t != null) {
                return t;
            }
            Map<String, Object> body = new LinkedHashMap<String, Object>();
            body.put(auth.getUsernameField(), auth.getUsername());
            body.put(auth.getPasswordField(), auth.getPassword());
            ApiRequest req = new ApiRequest(ApiMethod.POST, baseUrl + auth.getLoginPath());
            req.setBody(body);
            ApiResponse resp = client.send(req);
            if (resp.status() != 200) {
                throw new LightApiException("登录失败 status=" + resp.status()
                        + " url=" + req.getUrl() + " body=" + resp.bodyAsString());
            }
            String tokenValue = JsonPaths.readAsString(resp, auth.getTokenJsonPath());
            if (tokenValue == null) {
                throw new LightApiException("登录响应中未取到 token: " + auth.getTokenJsonPath()
                        + " body=" + resp.bodyAsString());
            }
            token.set(tokenValue);
            return tokenValue;
        } finally {
            loginLock.unlock();
        }
    }
}
