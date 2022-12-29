package com.neko233.db;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import static com.alibaba.druid.pool.DruidDataSourceFactory.*;
import static com.alibaba.druid.pool.DruidDataSourceFactory.PROP_MAXWAIT;

public interface DataSourceMock {

    @NotNull
    static Properties pullMysqlConfigProperties() {
        Properties dsConfig = new Properties();
        dsConfig.put(PROP_DRIVERCLASSNAME, "com.mysql.cj.jdbc.Driver");
        dsConfig.put(PROP_URL, "jdbc:mysql://localhost:3306/demo");
        dsConfig.put(PROP_USERNAME, "root");
        dsConfig.put(PROP_PASSWORD, "root");
        dsConfig.put(PROP_INITIALSIZE, "5");
        dsConfig.put(PROP_MINIDLE, "5");
        dsConfig.put(PROP_MAXACTIVE, "10");
        dsConfig.put(PROP_MAXWAIT, "30000");
        return dsConfig;
    }

    /**
     * @return DataSource
     */
    static DataSource createDataSource() {
        DataSource dataSource = null;
        try {
            dataSource = DruidDataSourceFactory.createDataSource(pullMysqlConfigProperties());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement("create database demo");
            ps.executeUpdate();
        } catch (SQLException e) {
        }

        return dataSource;
    }


}
