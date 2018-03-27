package com.example.upgrade.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseContext.class);

    private String datasourceDriver = "com.mysql.jdbc.Driver";
    private String datasourceUrl = "jdbc:mysql://127.0.0.1:3306/world?characterEncoding=utf8&useSSL=false";
    private String datasourceUsername = "root";
    private String datasourcePassword = "123456";

    public DatabaseContext(String datasourceDriver, String datasourceUrl, String datasourceUsername, String datasourcePassword) {
        this.datasourceDriver = datasourceDriver;
        this.datasourceUrl = datasourceUrl;
        this.datasourceUsername = datasourceUsername;
        this.datasourcePassword = datasourcePassword;
    }

    public Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName(this.datasourceDriver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            conn = DriverManager.getConnection(datasourceUrl, datasourceUsername, datasourcePassword);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conn;
    }

    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                conn = null;
            } catch (SQLException e) {
                LOGGER.error(String.format("Close Connection Exception %s",e));
            }
        }

    }


}
