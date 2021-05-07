package com.wmz.hbase.plugin.parse.convert;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 * @description:
 * @create: 2021-01-12
 **/
public interface Converter {
    byte[] valueToByte(Object obj);
    Object byteToValue(byte[] bytes, Type type);


    static  <T>  String convertToString(T obj){
        if(obj == null){
            return null;
        }
        Class clazz  =  obj.getClass();
        //对于Hbase Bytes类不支持的对象类型，统一用json转字符串处理
        return isHbaseSupportClassType(clazz) ? obj.toString() : JSONObject.toJSONString(obj);
    }

    static  boolean isHbaseSupportClassType(Type clazz){
        return clazz.equals(String.class) ||  clazz.equals(Integer.class) || clazz.equals(Integer.TYPE)
                ||  clazz.equals(BigDecimal.class)
                || clazz.equals(Boolean.class) || clazz.equals(Boolean.TYPE)
                || clazz.equals(Double.class) || clazz.equals(Double.TYPE)
                || clazz.equals(Float.class) || clazz.equals(Float.TYPE)
                || clazz.equals(Short.class) || clazz.equals(Short.TYPE)
                || isHbaseSuppertIncrementType(clazz);

    }

    static boolean isHbaseSuppertIncrementType(Type clazz){
        return clazz.equals(Long.class) || clazz.equals(Long.TYPE)  ;
    }
}
