package com.example.upgrade.pojo;

import java.util.ArrayList;
import java.util.List;

public class UpdateItem {
    private String version;
    List<UpdateBody> updateBody;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<UpdateBody> getUpdateBody() {
        return updateBody;
    }

    public void setUpdateBody(List<UpdateBody> updateBody) {
        this.updateBody = updateBody;
    }
}
