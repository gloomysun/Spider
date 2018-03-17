package com.ly.utils;

import com.alibaba.druid.pool.DruidDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnectionPool {
    private static volatile DBConnectionPool dbConnection;

    private DruidDataSource dataSource;

    /**
     * 构造函数中完成初始化
     */
    private DBConnectionPool() {

        try {
            System.out.println("初始化连接池");
            Properties properties = new Properties();
            InputStream in = ClassLoader.getSystemResourceAsStream("dataSource.properties");
            properties.load(in);
            String driverClassName = properties.getProperty("jdbc.driverClassName");
            String url = properties.getProperty("jdbc.url");
            String username = properties.getProperty("jdbc.username");
            String password = properties.getProperty("jdbc.password");
            dataSource = new DruidDataSource();
            dataSource.setDriverClassName(driverClassName);
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库连接对象，单例
     *
     * @return
     */
    public static DBConnectionPool getInstance() {
        if (dbConnection == null) {
            synchronized (DBConnectionPool.class) {
                if (dbConnection == null) {
                    dbConnection = new DBConnectionPool();
                }
            }
        }
        return dbConnection;
    }

    /**
     * 获取数据库连接
     *
     * @return 数据库连接
     */
    public final synchronized Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
