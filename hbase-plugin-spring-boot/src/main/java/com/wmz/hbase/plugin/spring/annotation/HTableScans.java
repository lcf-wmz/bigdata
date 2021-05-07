package com.wmz.hbase.plugin.spring.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @description: 该注解用来设置需要扫描HBaseTable注解类的包
 * @create: 2020-08-12
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({HTableScanRegistrar.RepeatingRegistrar.class})
public @interface HTableScans {
    HTableScan[] value();
}
