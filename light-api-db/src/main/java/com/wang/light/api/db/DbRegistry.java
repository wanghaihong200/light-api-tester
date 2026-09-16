package com.wang.light.api.db;

import com.wang.light.api.bootstrap.LightApiManager;
import com.wang.light.api.config.DataSourceConfig;
import com.wang.light.api.config.ProfileConfig;
import com.wang.light.api.exception.LightApiException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源注册表：按名字懒创建 HikariCP 数据源（并行安全）。
 * 配置只有一个数据源时可作为默认源；多个时必须指名。
 */
public final class DbRegistry {

    private static final ConcurrentHashMap<String, DbClient> CLIENTS = new ConcurrentHashMap<String, DbClient>();

    private DbRegistry() {
    }

    public static DbClient get(String name) {
        return CLIENTS.computeIfAbsent(name, n -> create(n));
    }

    /** 默认数据源：显式 default 键优先；否则唯一数据源兜底；多个且无 default 时报错指路 */
    public static DbClient defaultSource() {
        Map<String, DataSourceConfig> sources = LightApiManager.activeProfile().getDataSources();
        if (sources.containsKey("default")) {
            return get("default");
        }
        if (sources.size() == 1) {
            return get(sources.keySet().iterator().next());
        }
        throw new LightApiException("存在多个数据源且未配置 default，请用 Db.source(\"名字\") 指定。现有: " + sources.keySet());
    }

    private static DbClient create(String name) {
        ProfileConfig profile = LightApiManager.activeProfile();
        DataSourceConfig ds = profile.getDataSources().get(name);
        if (ds == null) {
            throw new LightApiException("数据源 '" + name + "' 未配置，现有: " + profile.getDataSources().keySet());
        }
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(ds.getUrl());
        hc.setUsername(ds.getUsername());
        hc.setPassword(ds.getPassword());
        if (ds.getDriverClassName() != null && !ds.getDriverClassName().trim().isEmpty()) {
            hc.setDriverClassName(ds.getDriverClassName());
        }
        hc.setMaximumPoolSize(ds.getMaximumPoolSize());
        hc.setPoolName("light-api-db-" + name);
        return new DbClient(new HikariDataSource(hc));
    }
}
