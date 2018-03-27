package com.example.upgrade.init;

import com.alibaba.fastjson.JSON;
import com.example.upgrade.pojo.UpdateItem;
import com.example.upgrade.pojo.UpdateItems;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * 数据库补丁升级的Service
 */
public class DatabasePatchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabasePatchService.class);

    /**
     * 升级文件地址
     */
    private static final String UPGRADE_FILE = "classpath:upgrade/UpgradeConfigFile.json";

    private  UpdateItems updateItems;

    private static DatabasePatchService service = null;

    private DatabasePatchService() {
        this.updateItems = getUpdateItems();
    }

    public static DatabasePatchService getInstance() {
        if (service == null) {
            service = new DatabasePatchService();
        }
        return service;
    }

    /**
     * 获取当前数据库的版本
     *
     * @return
     */
    public Float getCurrentDatabaseVersion(String datasourceDriver, String datasourceUrl, String datasourceUsername, String datasourcePassword) {

        String queryCurrentDatabaseVersion = String.format("SELECT * FROM %s WHERE %S", this.updateItems.getTableName(), this.updateItems.getQueryWhere());

        DatabaseContext context = new DatabaseContext(datasourceDriver, datasourceUrl, datasourceUsername, datasourcePassword);
        Connection conn = context.getConnection();

        Statement statement = null;
        ResultSet rs = null;
        String currentVersion = null;
        try {
            statement = conn.createStatement();
            rs = statement.executeQuery(queryCurrentDatabaseVersion);

            while (rs.next()){
                currentVersion = rs.getString(this.updateItems.getFieldName());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
               if (rs != null){
                   rs.close();
                   rs = null;
               }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                if (statement != null){
                    statement.close();
                    statement = null;
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            context.closeConnection(conn);
            conn = null;
        }

        return StringUtils.isBlank(currentVersion) ? -1F : Float.parseFloat(currentVersion);
    }

    /**
     * 获取升级文件
     *
     * @return
     */
    private UpdateItems getUpdateItems() {

        UpdateItems updateItems = null;

        try {
            File file = ResourceUtils.getFile(UPGRADE_FILE);
            InputStream inputStream = new FileInputStream(file);
            String text = IOUtils.toString(inputStream, "utf8");
            updateItems = JSON.parseObject(text, UpdateItems.class);
        } catch (Exception e) {
            LOGGER.info("获取升级文件失败");
        }

        if (updateItems == null) {
            return null;
        }


        List<UpdateItem> list = updateItems.getUpdateItem();

        if (list != null && list.size() > 0) {
            Collections.sort(list, new Comparator<UpdateItem>() {
                @Override
                public int compare(UpdateItem o1, UpdateItem o2) {
                    float i = Float.parseFloat(o1.getVersion()) - Float.parseFloat(o2.getVersion());
                    return (i == 0 ? 0 : (i < 0 ? -1 : 1));
                }
            });
        }

        return updateItems;
    }

    /**
     * 判断是否为新的版本
     *
     * @param jsonVersion
     * @param currentVersion
     * @return
     */
    private boolean isNewVersion(float jsonVersion, float currentVersion) {
        return (jsonVersion - currentVersion <= 0 ? false : true);
    }


    public void updateDatabase(Float currentVersion, String datasourceDriver, String datasourceUrl, String datasourceUsername, String datasourcePassword) {
        // 获取更新文件的所有内容
        //UpdateItems updateItems = getUpdateItems();
        UpdateItems updateItems = this.updateItems;

        if (updateItems == null) {
            return;
        }

        List<UpdateItem> list = updateItems.getUpdateItem();

        if (list == null || list.size() <= 0) {
            // 表示为空，证明没有最新版本可供升级，直接返回true
            return;
        }

        List<UpdateItem> updateItemList = new ArrayList<UpdateItem>();

        for (UpdateItem updateItem : list) {
            if (Float.parseFloat(updateItem.getVersion()) > currentVersion) {
                updateItemList.add(updateItem);
            }
        }

        if (updateItemList.size() > 0) {
            LOGGER.info("检测到数据库版本有更新，升级开始");
            updateItems.setUpdateItem(updateItemList);
            //执行升级
            doAction(updateItems, datasourceDriver, datasourceUrl, datasourceUsername, datasourcePassword);
        } else {
            LOGGER.info("数据库版本暂无更新");
            return;
        }


    }

    /**
     * 执行升级
     *
     * @param updateItems
     * @param datasourceDriver
     * @param datasourceUrl
     * @param datasourceUsername
     * @param datasourcePassword
     */
    private void doAction(UpdateItems updateItems, String datasourceDriver, String datasourceUrl, String datasourceUsername, String datasourcePassword) {
        UpdateTask task = new UpdateTask(datasourceDriver, datasourceUrl, datasourceUsername, datasourcePassword);
        task.addAll(updateItems);
        task.doAction();
    }


}
