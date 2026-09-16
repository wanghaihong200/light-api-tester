package com.wang.light.example;

import com.wang.light.api.db.Db;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DB 模块演示：直接写 SQL 做落库校验；无 Docker 自动跳过。
 */
@SpringBootTest(classes = DemoApp.class)
public class DbCheckSpringTest extends AbstractTestNGSpringContextTests {

    @Test
    public void crudWithDirectSql() {
        if (!DemoSuiteListener.isDbAvailable()) {
            throw new SkipException("Docker/MySQL 容器不可用，跳过 DB 用例");
        }
        Db.update("CREATE TABLE IF NOT EXISTS light_demo (id INT PRIMARY KEY, name VARCHAR(50))");
        Db.update("INSERT INTO light_demo (id, name) VALUES (?, ?)", 1, "tom-db");

        List<Map<String, Object>> rows = Db.query("SELECT id, name FROM light_demo WHERE id = ?", 1);
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("name")).isEqualTo("tom-db");

        Integer count = Db.queryOne("SELECT COUNT(*) AS c FROM light_demo")
                .values().iterator().next() == null ? 0 : 1;
        assertThat(count).isGreaterThanOrEqualTo(1);
    }
}
