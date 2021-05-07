package com.wmz.hbase.plugin.exception;

import java.lang.reflect.Method;

/**
 * @description:
 * @create: 2021-01-08
 **/
public class ParseHbaseFieldAnnoMethodException extends RuntimeException{

    public ParseHbaseFieldAnnoMethodException(String name){
        super("Error parsing method  '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
    }
}
