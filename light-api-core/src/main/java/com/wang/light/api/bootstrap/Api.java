package com.wang.light.api.bootstrap;

import com.wang.light.api.client.ApiClient;

/**
 * 业务脚本的静态门面入口。
 */
public final class Api {

    private Api() {
    }

    /** 默认客户端（配置的默认环境） */
    public static ApiClient client() {
        return LightApiManager.defaultClient();
    }

    /** 按环境名取独立客户端（多系统/多账号） */
    public static ApiClient client(String envName) {
        return LightApiManager.createClient(envName);
    }
}
