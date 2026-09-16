package com.wang.light.api.db;

import java.util.List;
import java.util.Map;

/**
 * DB 模块静态门面：Db.query/update/... 直接写 SQL 操作 MySQL（默认数据源）；
 * 多数据源用 Db.source("名字")。
 */
public final class Db {

    private Db() {
    }

    public static DbClient source() {
        return DbRegistry.defaultSource();
    }

    public static DbClient source(String name) {
        return DbRegistry.get(name);
    }

    public static List<Map<String, Object>> query(String sql, Object... params) {
        return source().query(sql, params);
    }

    public static <T> List<T> query(String sql, Class<T> type, Object... params) {
        return source().query(sql, type, params);
    }

    public static Map<String, Object> queryOne(String sql, Object... params) {
        return source().queryOne(sql, params);
    }

    public static <T> T queryOne(String sql, Class<T> type, Object... params) {
        return source().queryOne(sql, type, params);
    }

    public static int update(String sql, Object... params) {
        return source().update(sql, params);
    }
}
