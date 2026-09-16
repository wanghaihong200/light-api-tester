package com.wang.light.api.report;

import com.wang.light.api.client.ApiRequest;
import com.wang.light.api.client.ApiResponse;
import com.wang.light.api.spi.Filter;
import io.qameta.allure.Allure;

/**
 * 请求/响应现场自动附到 Allure 步骤（Filter SPI 实现）。
 * Allure 不在 classpath 时自动降级为直通（可选依赖设计）。
 */
public class AllureRequestLogFilter implements Filter {

    private static final boolean ALLURE_PRESENT = detectAllure();

    private static boolean detectAllure() {
        try {
            Class.forName("io.qameta.allure.Allure", false, AllureRequestLogFilter.class.getClassLoader());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public ApiRequest beforeRequest(ApiRequest request) {
        if (!ALLURE_PRESENT) {
            return request;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(request.getMethod()).append(' ').append(request.getUrl()).append('\n');
        sb.append(request.getHeaders()).append('\n');
        if (!request.getQuery().isEmpty()) {
            sb.append("query: ").append(request.getQuery()).append('\n');
        }
        if (request.getBody() != null) {
            sb.append("body: ").append(String.valueOf(request.getBody()));
        }
        Allure.addAttachment("request", "text/plain", sb.toString());
        return request;
    }

    @Override
    public ApiResponse afterResponse(ApiResponse response) {
        if (!ALLURE_PRESENT) {
            return response;
        }
        String text = "status=" + response.status() + " (" + response.durationMs() + "ms)\n"
                + response.bodyAsString();
        Allure.addAttachment("response", "text/plain", text);
        return response;
    }
}
