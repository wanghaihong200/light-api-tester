package com.wang.light.api.db;

import com.wang.light.api.config.ProfileConfig;
import com.wang.light.api.exception.LightApiException;
import com.wang.light.api.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 单数据源的 MySQL CRUD 客户端：测试人员直接写 SQL。
 * 行默认转 LinkedHashMap（保持列序）；传 Class 则按 Jackson 映射成 POJO。
 * SQL、参数与结果数据随 profile 的 log-body 开关打印（超过 max-log-body-length 截断）。
 */
public class DbClient {

    private static final Logger log = LoggerFactory.getLogger(DbClient.class);

    private final DataSource dataSource;
    private final ProfileConfig profile;

    public DbClient(DataSource dataSource) {
        this(dataSource, new ProfileConfig());
    }

    public DbClient(DataSource dataSource, ProfileConfig profile) {
        this.dataSource = dataSource;
        this.profile = profile;
    }

    /** 查询多行，参数用 ? 占位符按序填充 */
    public List<Map<String, Object>> query(String sql, Object... params) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        long start = System.currentTimeMillis();
        logSql(sql, params);
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement(sql);
            bind(ps, params);
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int n = md.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<String, Object>();
                for (int i = 1; i <= n; i++) {
                    row.put(md.getColumnLabel(i), rs.getObject(i));
                }
                rows.add(row);
            }
            if (profile.isLogBody()) {
                log.info("[light-api-db] << {} rows ({}ms) {}",
                        rows.size(), System.currentTimeMillis() - start, truncate(Json.toJson(rows)));
            }
            return rows;
        } catch (Exception e) {
            throw new LightApiException("SQL 查询失败: " + sql, e);
        } finally {
            closeQuietly(rs, ps, con);
        }
    }

    /** 查询单行，无结果返回 null */
    public Map<String, Object> queryOne(String sql, Object... params) {
        List<Map<String, Object>> rows = query(sql, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** 查询多行并映射为 POJO 列表 */
    public <T> List<T> query(String sql, Class<T> type, Object... params) {
        List<Map<String, Object>> rows = query(sql, params);
        List<T> result = new ArrayList<T>(rows.size());
        for (Map<String, Object> row : rows) {
            result.add(Json.convert(row, type));
        }
        return result;
    }

    /** 查询单个 POJO，无结果返回 null */
    public <T> T queryOne(String sql, Class<T> type, Object... params) {
        List<T> rows = query(sql, type, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** 执行 DML/DDL（insert/update/delete/DDL），返回影响行数 */
    public int update(String sql, Object... params) {
        long start = System.currentTimeMillis();
        logSql(sql, params);
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement(sql);
            bind(ps, params);
            int affected = ps.executeUpdate();
            if (profile.isLogBody()) {
                log.info("[light-api-db] << affected={} ({}ms)", affected, System.currentTimeMillis() - start);
            }
            return affected;
        } catch (Exception e) {
            throw new LightApiException("SQL 执行失败: " + sql, e);
        } finally {
            closeQuietly(null, ps, con);
        }
    }

    private void logSql(String sql, Object[] params) {
        if (profile.isLogBody()) {
            log.info("[light-api-db] >> {} | params={}", sql, Arrays.asList(params));
        }
    }

    private String truncate(String text) {
        return profile.getMaxLogBodyLength() >= 0 && text.length() > profile.getMaxLogBodyLength()
                ? text.substring(0, profile.getMaxLogBodyLength()) + "...(截断)"
                : text;
    }

    private void bind(PreparedStatement ps, Object[] params) throws Exception {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    private void closeQuietly(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            if (r != null) {
                try {
                    r.close();
                } catch (Exception ignore) {
                }
            }
        }
    }
}
