package com.wmz.hbase.plugin.exception;

/**
 * @description: 对于未配置@HbaseTable注解的类，进行hbase plugin相关映射操作，将抛出该异常
 * @create: 2020-05-28
 **/
public class ClassNotExistAnnoHbaseTableException extends RuntimeException{

    public ClassNotExistAnnoHbaseTableException(Class clazz){
        super(clazz.getName()+"not use anno @HbaseTable");
    }

}
