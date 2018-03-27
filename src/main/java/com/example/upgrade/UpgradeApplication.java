package com.example.upgrade;


import com.alibaba.fastjson.JSON;
import com.example.upgrade.pojo.UpdateItems;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.util.ResourceUtils;


import java.io.*;

@SpringBootApplication
@ServletComponentScan
public class UpgradeApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(UpgradeApplication.class, args);
	}
}
