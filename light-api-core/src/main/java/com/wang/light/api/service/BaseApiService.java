package com.wang.light.api.service;

import com.wang.light.api.bootstrap.LightApiManager;
import com.wang.light.api.client.ApiClient;
import com.wang.light.api.client.RequestSpec;

/**
 * 官方推荐的业务分层基类（只做便利封装，不做重机制）。
 * 业务工程继承它，把每个接口封装成一个方法：`UserApiService.login(x) -> LoginResponse`。
 */
public abstract class BaseApiService {
    private final ApiClient client;

    protected BaseApiService() {
        this(LightApiManager.defaultClient());
    }

    protected BaseApiService(ApiClient client) {
        this.client = client;
    }

    protected ApiClient client() {
        return client;
    }

    protected RequestSpec get(String path) {
        return client.get(path);
    }

    protected RequestSpec post(String path) {
        return client.post(path);
    }

    protected RequestSpec put(String path) {
        return client.put(path);
    }

    protected RequestSpec patch(String path) {
        return client.patch(path);
    }

    protected RequestSpec delete(String path) {
        return client.delete(path);
    }
}
