package com.example.upgrade.pojo;

import java.util.List;

public class UpdateItems {
    private Boolean isUpdateDbVersion = true;
    private String tableName = "t_system_info";
    private String fieldName = "param_value";
    private String queryWhere = "param='DBVersion'";
    private List<UpdateItem> updateItem;

    public Boolean getUpdateDbVersion() {
        return isUpdateDbVersion;
    }

    public void setUpdateDbVersion(Boolean updateDbVersion) {
        isUpdateDbVersion = updateDbVersion;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getQueryWhere() {
        return queryWhere;
    }

    public void setQueryWhere(String queryWhere) {
        this.queryWhere = queryWhere;
    }

    public List<UpdateItem> getUpdateItem() {
        return updateItem;
    }

    public void setUpdateItem(List<UpdateItem> updateItem) {
        this.updateItem = updateItem;
    }
}
