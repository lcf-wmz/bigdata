package com.wmz.hbase.plugin.exception;

/**
 * @description: class中没有任何field 被@HbaseField 注解，抛出该异常
 * @create: 2020-10-14
 **/
public class ClassNotExistAnnoRowKeyException extends RuntimeException {

    public ClassNotExistAnnoRowKeyException(Class clazz){
        super(clazz.getName() + " not exist anno field  by @HbaseRowKey");
    }
}
