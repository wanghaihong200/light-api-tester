package com.wang.light.api.db;

import com.wang.light.api.exception.LightApiException;
import com.wang.light.api.util.Json;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 单数据源的 MySQL CRUD 客户端：测试人员直接写 SQL。
 * 行默认转 LinkedHashMap（保持列序）；传 Class 则按 Jackson 映射成 POJO。
 */
public class DbClient {

    private final DataSource dataSource;

    public DbClient(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** 查询多行，参数用 ? 占位符按序填充 */
    public List<Map<String, Object>> query(String sql, Object... params) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
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
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement(sql);
            bind(ps, params);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new LightApiException("SQL 执行失败: " + sql, e);
        } finally {
            closeQuietly(null, ps, con);
        }
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
