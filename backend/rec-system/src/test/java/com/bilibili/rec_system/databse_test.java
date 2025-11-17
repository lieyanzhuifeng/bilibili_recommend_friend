package com.bilibili.rec_system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class database_test {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void printAllTablesAndColumns() throws SQLException {
        System.out.println("=== 数据库表结构分析 ===");

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            // 获取所有表
            String catalog = connection.getCatalog(); // 当前数据库
            String schema = metaData.getUserName();   // 模式名

            System.out.println("数据库: " + catalog);
            System.out.println("模式: " + schema);
            System.out.println();

            // 获取表信息
            ResultSet tables = metaData.getTables(catalog, schema, "%", new String[]{"TABLE"});

            List<String> tableNames = new ArrayList<>();
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                tableNames.add(tableName);

                System.out.println("📊 表名: " + tableName);
                System.out.println("   描述: " + tables.getString("REMARKS"));

                // 获取该表的所有列
                printTableColumns(metaData, catalog, schema, tableName);
                System.out.println("────────────────────────────────────────");
            }

            if (tableNames.isEmpty()) {
                System.out.println("❌ 数据库中没有找到任何表");
            } else {
                System.out.println("✅ 共找到 " + tableNames.size() + " 张表: " + tableNames);
            }

        } catch (Exception e) {
            System.err.println("❌ 获取数据库结构失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printTableColumns(DatabaseMetaData metaData, String catalog, String schema, String tableName)
            throws SQLException {

        ResultSet columns = metaData.getColumns(catalog, schema, tableName, "%");

        System.out.println("   字段列表:");
        boolean hasColumns = false;

        while (columns.next()) {
            hasColumns = true;
            String columnName = columns.getString("COLUMN_NAME");
            String dataType = columns.getString("TYPE_NAME");
            int columnSize = columns.getInt("COLUMN_SIZE");
            String isNullable = columns.getString("IS_NULLABLE");
            String isAutoIncrement = columns.getString("IS_AUTOINCREMENT");
            String columnDefault = columns.getString("COLUMN_DEF");

            System.out.printf("     ├─ %-20s %-15s (长度: %-4s) ",
                    columnName, dataType, columnSize);

            // 添加额外信息
            List<String> extras = new ArrayList<>();
            if ("YES".equals(isNullable)) extras.add("可空");
            if ("YES".equals(isAutoIncrement)) extras.add("自增");
            if (columnDefault != null) extras.add("默认值: " + columnDefault);

            if (!extras.isEmpty()) {
                System.out.print("[" + String.join(", ", extras) + "]");
            }
            System.out.println();
        }

        if (!hasColumns) {
            System.out.println("     └─ 无字段");
        }

        // 获取主键信息
        printPrimaryKeys(metaData, catalog, schema, tableName);
    }

    private void printPrimaryKeys(DatabaseMetaData metaData, String catalog, String schema, String tableName)
            throws SQLException {

        ResultSet primaryKeys = metaData.getPrimaryKeys(catalog, schema, tableName);
        List<String> pkColumns = new ArrayList<>();

        while (primaryKeys.next()) {
            pkColumns.add(primaryKeys.getString("COLUMN_NAME"));
        }

        if (!pkColumns.isEmpty()) {
            System.out.println("   主键: " + String.join(", ", pkColumns));
        }
    }

    @Test
    void printSimpleTableInfo() throws SQLException {
        System.out.println("=== 简化的表信息 ===");

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            String schema = metaData.getUserName();

            ResultSet tables = metaData.getTables(catalog, schema, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("\n表: " + tableName);

                // 简单查询表的前几条数据来了解结构
                try {
                    jdbcTemplate.queryForList("SELECT * FROM " + tableName + " LIMIT 1")
                            .forEach(row -> {
                                System.out.println("  样例数据列:");
                                row.forEach((key, value) ->
                                        System.out.printf("    ├─ %-20s : %s%n", key, value)
                                );
                            });
                } catch (Exception e) {
                    System.out.println("   ❌ 无法查询表数据: " + e.getMessage());
                }
            }
        }
    }
}