package com.wang.light.api.spi;

import com.wang.light.api.client.ApiRequest;
import com.wang.light.api.client.ApiResponse;

/**
 * 请求/响应过滤器 SPI：签名、公共 header、埋点、失败落盘等横切逻辑的挂载点。
 * 实现必须无状态且线程安全（并行执行下被所有客户端共享）。
 */
public interface Filter {

    /** 请求发出前调用；可返回加工后的新请求对象 */
    default ApiRequest beforeRequest(ApiRequest request) {
        return request;
    }

    /** 响应返回后调用（逆序）；可返回包装后的新响应 */
    default ApiResponse afterResponse(ApiResponse response) {
        return response;
    }
}
