package com.wang.light.api.publish;

import java.util.ArrayList;
import java.util.List;

/**
 * 套件级结果汇总（推送给测试平台的数据模型，ADR-0005）。
 * 字段为占位契约，平台侧契约定稿后按需扩展。
 */
public class TestSummary {

    private String env;
    private String suiteName;
    private long startedAt;
    private long finishedAt;
    private int total;
    private int passed;
    private int failed;
    private int skipped;
    private List<TestCaseResult> cases = new ArrayList<TestCaseResult>();

    public String getEnv() { return env; }
    public void setEnv(String env) { this.env = env; }

    public String getSuiteName() { return suiteName; }
    public void setSuiteName(String suiteName) { this.suiteName = suiteName; }

    public long getStartedAt() { return startedAt; }
    public void setStartedAt(long startedAt) { this.startedAt = startedAt; }

    public long getFinishedAt() { return finishedAt; }
    public void setFinishedAt(long finishedAt) { this.finishedAt = finishedAt; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getPassed() { return passed; }
    public void setPassed(int passed) { this.passed = passed; }

    public int getFailed() { return failed; }
    public void setFailed(int failed) { this.failed = failed; }

    public int getSkipped() { return skipped; }
    public void setSkipped(int skipped) { this.skipped = skipped; }

    public List<TestCaseResult> getCases() { return cases; }
    public void setCases(List<TestCaseResult> cases) { this.cases = cases; }
}