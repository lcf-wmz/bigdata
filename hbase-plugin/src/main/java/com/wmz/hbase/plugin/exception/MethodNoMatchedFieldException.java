package com.wmz.hbase.plugin.exception;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @description:
 * @create: 2021-01-04
 **/
public class MethodNoMatchedFieldException extends RuntimeException{

    public MethodNoMatchedFieldException(Method method){
        super("class " + method.getDeclaringClass().getName() +"."+method.getName()+" not matched Field");
    }
}
