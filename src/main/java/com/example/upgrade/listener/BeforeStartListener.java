package com.example.upgrade.listener;

import com.example.upgrade.init.DatabasePatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class BeforeStartListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeforeStartListener.class);


    @Value("${spring.datasource.driver-class-name}")
    private String datasourceDriver;
    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    @Value("${spring.datasource.username}")
    private String datasourceUsername;
    @Value("${spring.datasource.password}")
    private String datasourcePassword;


    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LOGGER.info("Check Database Version");
        Float currentVersion = DatabasePatchService.getInstance().getCurrentDatabaseVersion(this.datasourceDriver, this.datasourceUrl, this.datasourceUsername, this.datasourcePassword);
        LOGGER.info(String.format("当前数据库版本为： %s" , currentVersion));
        DatabasePatchService.getInstance().updateDatabase(currentVersion,this.datasourceDriver,this.datasourceUrl,this.datasourceUsername,this.datasourcePassword);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
