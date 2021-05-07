package com.wmz.hbase.plugin.exception;

import java.lang.reflect.Field;

/**
 * @description: class中没有任何field 被@HbaseField 注解，抛出该异常
 * @create: 2020-10-14
 **/
public class ClassNotExistAnnoFiledException extends RuntimeException {

    public ClassNotExistAnnoFiledException(Class clazz){
        super(clazz.getName() + " not exist anno field  by @HbaseField");
    }
}
