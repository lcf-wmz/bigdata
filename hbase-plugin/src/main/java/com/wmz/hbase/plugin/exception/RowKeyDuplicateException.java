package com.wmz.hbase.plugin.exception;

/**
 * @description:
 * @create: 2021-01-04
 **/
public class RowKeyDuplicateException extends RuntimeException{

    public RowKeyDuplicateException(Class clazz){
        super(clazz.getName()+" duplicate @HbaseRowKey");
    }
}
