package com.wmz.hbase.plugin.parse.convert;

/**
 * @description:
 * @create: 2021-01-12
 **/

import com.alibaba.fastjson.JSONObject;
import org.apache.hadoop.hbase.util.Bytes;

import java.lang.reflect.Type;
import java.math.BigDecimal;

public class RowKeyConverter implements Converter{

    private RowKeyConverter(){}

    private static class RowKeyConvertHolder{
        private static final RowKeyConverter INSTANCE = new RowKeyConverter();
    }

    public static RowKeyConverter getInstance(){
        return RowKeyConvertHolder.INSTANCE;
    }

    @Override
    public byte[] valueToByte(Object obj) {
        String result = Converter.convertToString(obj);
        return result == null ? null : Bytes.toBytes(result);
    }

    @Override
    public Object byteToValue(byte[] bytes, Type type) {
        if(bytes == null){
            return null;
        }
        String str= Bytes.toString(bytes);
        if (type.equals(String.class)) {
            return str;
        }
        if (type.equals(BigDecimal.class)) {
            return new BigDecimal(str);
        }
        if(type.equals(Boolean.class) || type.equals(Boolean.TYPE)){
            return Boolean.valueOf(str);
        }
        if (type.equals(Long.class) || type.equals(Long.TYPE)) {
            return Long.valueOf(str);
        }
        if (type.equals(Double.class) || type.equals(Double.TYPE)) {
            return Double.valueOf(str);
        }
        if (type.equals(Float.class) || type.equals(Float.TYPE)) {
            return Float.valueOf(str);
        }
        if (type.equals(Short.class) || type.equals(Short.TYPE)) {
            return Short.valueOf(str);
        }
        if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
            return Integer.valueOf(str);
        }
        //对于Hbase Bytes类不支持的对象类型，统一用json转字符串处理
        return JSONObject.parseObject(str,type);
    }
}