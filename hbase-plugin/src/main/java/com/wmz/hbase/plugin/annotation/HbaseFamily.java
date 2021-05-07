package com.wmz.hbase.plugin.annotation;


import org.apache.hadoop.hbase.io.compress.Compression;

import java.lang.annotation.*;

/**
 * @description:
 * @create: 2020-05-18
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited()
public @interface HbaseFamily {
    //列簇名
    String family() default "f";
    //声明过期时间，默认永不过期
    String expire() default "15 DAYS"; //"90 DAYS","90 SECONDS","90 HOURS","90 MINUTES","FOREVER"
    //压缩格式
    Compression.Algorithm compressionType() default Compression.Algorithm.SNAPPY;

}
