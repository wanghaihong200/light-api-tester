package com.wang.light.api.spi;

import com.wang.light.api.publish.TestSummary;

/**
 * 结果发布器 SPI（ADR-0005：框架只推送，渲染归测试平台）。
 * 套件执行结束后由 LightApiListener 调用；实现自行判断配置开关。
 * 实现必须容错：推送失败只允许记日志，不允许影响测试退出码。
 */
public interface ResultPublisher {

    void publish(TestSummary summary);
}
