package com.wang.light.api.publish;

/**
 * 单用例结果（推送给测试平台的最小明细）。
 */
public class TestCaseResult {

    public static final String STATUS_PASSED = "PASSED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_SKIPPED = "SKIPPED";

    private String name;
    private String className;
    private String status;
    private long durationMs;
    private String message;

    public TestCaseResult() {
    }

    public TestCaseResult(String name, String className, String status, long durationMs, String message) {
        this.name = name;
        this.className = className;
        this.status = status;
        this.durationMs = durationMs;
        this.message = message;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
