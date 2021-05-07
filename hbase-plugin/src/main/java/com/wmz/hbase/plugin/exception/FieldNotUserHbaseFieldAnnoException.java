package com.wmz.hbase.plugin.exception;

import java.lang.reflect.Field;

/**
 * @description: 对于未配置@HbaseField的field，进行hbase plugin相关映射操作，将抛出该异常
 * @create: 2020-05-28
 **/
public class FieldNotUserHbaseFieldAnnoException extends RuntimeException{

    public FieldNotUserHbaseFieldAnnoException(Field field){
        super("class " + field.getDeclaringClass().getName() +"."+field.getName()+" not use anno @HbaseField");
    }

}
