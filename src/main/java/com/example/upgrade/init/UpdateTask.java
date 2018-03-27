package com.example.upgrade.init;

import com.example.upgrade.pojo.UpdateBody;
import com.example.upgrade.pojo.UpdateItem;
import com.example.upgrade.pojo.UpdateItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UpdateTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateTask.class);

    private String datasourceDriver;
    private String datasourceUrl;
    private String datasourceUsername;
    private String datasourcePassword;
    private DatabaseContext context = null;
    private UpdateItems updateItems = null;


    public UpdateTask(String datasourceDriver, String datasourceUrl, String datasourceUsername, String datasourcePassword) {
        this.datasourceDriver = datasourceDriver;
        this.datasourceUrl = datasourceUrl;
        this.datasourceUsername = datasourceUsername;
        this.datasourcePassword = datasourcePassword;
        initDatabaseContext();
    }

    private void initDatabaseContext() {
        if (this.context == null) {
            this.context = new DatabaseContext(this.datasourceDriver, this.datasourceUrl, this.datasourceUsername, this.datasourcePassword);
        }
    }


    public void addAll(UpdateItems updateItems) {
        this.updateItems = updateItems;
    }


    public Boolean doAction() {

        List<UpdateItem> updateItemList = this.updateItems.getUpdateItem();
        if (updateItemList == null || updateItemList.size() <= 0) {
            return false;
        }

        Connection conn = this.context.getConnection();

        try {
            for (UpdateItem updateItem : updateItemList) {
                LOGGER.info(String.format("正在升级，版本为： %s", updateItem.getVersion()));
                List<UpdateBody> bodyList = updateItem.getUpdateBody();
                for (UpdateBody updateBody : bodyList) {
                    if (updateBody.getType().equalsIgnoreCase("SqlStatement")) {
                        // sql表达式
                        executeSqlStatement(conn, updateBody);
                    } else if (updateBody.getType().equalsIgnoreCase("SqlFile")) {
                        // sql文件
                        executeSqlFile(conn, updateBody);
                    } else if (updateBody.getType().equalsIgnoreCase("SqlFolder")) {
                        // TODO 文件目录
                        executeSqlFolder(conn, updateBody);
                    } else if (updateBody.getType().equalsIgnoreCase("BatchFile")) {
                        //  TODO 批处理文件
                        executeBatchFile(conn, updateBody);
                    }
                }
                // 升级数据库版本
                executeUpdateSystemInfo(conn, updateItem, this.updateItems);
            }

            LOGGER.info("升级成功！");
        } catch (Exception e) {
            LOGGER.info("升级失败!");
        } finally {
            this.context.closeConnection(conn);
        }

        return false;
    }


    /**
     * 升级当前数据库版本
     *
     * @param conn
     * @param updateItem
     */
    private void executeUpdateSystemInfo(Connection conn, UpdateItem updateItem, UpdateItems updateItems) throws Exception {
        String ipAddress = InetAddress.getLocalHost().getHostAddress();
        String update_system_info_sql = String.format("UPDATE %s SET %s = ? , update_time = ? , ip_address = ? WHERE %s;", updateItems.getTableName(), updateItems.getFieldName(), updateItems.getQueryWhere());
        PreparedStatement ps = conn.prepareStatement(update_system_info_sql);
        ps.setString(1, updateItem.getVersion());
        ps.setTimestamp(2, new Timestamp(new Date().getTime()));
        ps.setString(3, ipAddress);
        ps.executeUpdate();
        conn.commit();
        try {
            ps.close();
            ps = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 执行sql表达式的方法
     *
     * @param conn
     * @param updateBody
     */
    private void executeSqlStatement(Connection conn, UpdateBody updateBody) throws SQLException {
        conn.setAutoCommit(false);
        Statement statement = conn.createStatement();
        statement.execute(updateBody.getText());
        conn.commit();
        try {
            statement.close();
            statement = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行sql文件的方法
     *
     * @param conn
     * @param updateBody
     */
    private void executeSqlFile(Connection conn, UpdateBody updateBody) throws Exception {

        List<String> sqlFile = loadSqlFile(updateBody.getText());
        conn.setAutoCommit(false);
        Statement statement = conn.createStatement();
        if (sqlFile.size() > 1) {
            for (String sql : sqlFile) {
                statement.addBatch(sql);
            }
            statement.executeBatch();
        } else {
            statement.executeUpdate((String) sqlFile.get(0));
        }
        conn.commit();

        try {
            statement.close();
            statement = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 寻找sql文件夹，执行
     *
     * @param conn
     * @param updateBody
     */
    private void executeSqlFolder(Connection conn, UpdateBody updateBody) throws SQLException {
       // conn.setAutoCommit(false);
    }

    /**
     * 执行sql批处理
     *
     * @param conn
     * @param updateBody
     */
    private void executeBatchFile(Connection conn, UpdateBody updateBody) throws SQLException {
        //conn.setAutoCommit(false);
    }


    private List<String> loadSqlFile(String path) throws Exception {
        List<String> sqlList = new ArrayList<>();

        StringBuffer sourcePathBuffer = new StringBuffer();
        sourcePathBuffer.append("classpath:upgrade").append(path);
        String sourcePath = sourcePathBuffer.toString();

        InputStream sqlFileIn = null;

        try {

            File file = ResourceUtils.getFile(sourcePath);
            sqlFileIn = new FileInputStream(file);

            StringBuffer sqlBuffer = new StringBuffer();
            byte[] buff = new byte['?'];
            int byteRead = 0;
            while ((byteRead = sqlFileIn.read(buff)) != -1) {
                sqlBuffer.append(new String(buff, 0, byteRead, "UTF-8"));
            }

            File tempFile = new File(sourcePath.trim());
            String fileName = tempFile.getName().toUpperCase();
            if ((fileName.startsWith("PROC_")) || (fileName.startsWith("FUNC_"))) {
                sqlList.add(sqlBuffer.toString());
            } else {
                String[] sqlArr = sqlBuffer.toString().split("(;\\s*\\r\\n)|(;\\s*\\n)");
                for (int i = 0; i < sqlArr.length; i++) {
                    String sql = sqlArr[i].replaceAll("--.*", "").trim();
                    if (!sql.equals("")) {
                        if (sql.endsWith(";"))
                            sql = sql.substring(0, sql.length() - 1);
                        sqlList.add(sql);
                    }
                }
            }
            return sqlList;
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        } finally {
            if (sqlFileIn != null) {
                sqlFileIn.close();
            }
        }
    }


}
