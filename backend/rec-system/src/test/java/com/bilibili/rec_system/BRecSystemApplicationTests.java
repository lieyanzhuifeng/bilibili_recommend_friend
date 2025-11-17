package com.bilibili.rec_system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Map;

@SpringBootTest
class BRecSystemApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void testDatabaseConnection() {
        System.out.println("=== 测试数据库连接 ===");

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            System.out.println("数据库: " + metaData.getDatabaseProductName());
            System.out.println("URL: " + metaData.getURL());
            System.out.println("用户: " + metaData.getUserName());
            System.out.println("驱动: " + metaData.getDriverName());

        } catch (Exception e) {
            System.err.println("数据库连接失败: " + e.getMessage());
        }
    }

    @Test
    void testDatabaseQuery() {
        System.out.println("=== 测试数据库查询 ===");

        try {
            // 测试当前时间
            String time = jdbcTemplate.queryForObject("SELECT NOW()", String.class);
            System.out.println("数据库时间: " + time);

            // 显示所有表
            List<Map<String, Object>> tables = jdbcTemplate.queryForList("SHOW TABLES");
            System.out.println("表数量: " + tables.size());
            tables.forEach(table ->
                    System.out.println("表: " + table.values())
            );

        } catch (Exception e) {
            System.err.println("查询失败: " + e.getMessage());
        }
    }



}