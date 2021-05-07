package com.wmz.hbase.plugin.test;

import com.wmz.hbase.plugin.annotation.HbaseField;
import com.wmz.hbase.plugin.test.example.entity.Order;
import com.wmz.hbase.plugin.util.HbasePluginAnnoUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @create: 2020-06-02
 **/
@SpringBootApplication
public class HbasePluginSpringTestApplication {


    public static void main(String[] args) {
        SpringApplication.run(HbasePluginSpringTestApplication.class, args);
    }
}
