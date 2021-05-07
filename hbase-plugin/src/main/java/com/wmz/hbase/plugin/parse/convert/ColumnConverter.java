package com.wmz.hbase.plugin.parse.convert;

import com.alibaba.fastjson.JSONObject;
import org.apache.hadoop.hbase.util.Bytes;

import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 * @description:
 * @create: 2021-01-12
 **/
public class ColumnConverter implements Converter{

    private ColumnConverter(){}

    private static class ColumnConvertHolder{
        private static final ColumnConverter INSTANCE = new ColumnConverter();
    }

    public static ColumnConverter getInstance(){
        return ColumnConvertHolder.INSTANCE;
    }

    @Override
    public byte[] valueToByte(Object obj) {
        if(obj == null){
            return null;
        }
        if(obj instanceof String){
            return Bytes.toBytes((String)obj);
        }
        if(obj instanceof Integer){
            return Bytes.toBytes((Integer)obj);
        }
        if(obj instanceof Long){
            return Bytes.toBytes((Long)obj);
        }
        if(obj instanceof Boolean){
            return Bytes.toBytes((Boolean)obj);
        }
        if(obj instanceof BigDecimal){
            return Bytes.toBytes((BigDecimal)obj);
        }
        if(obj instanceof Double){
            return Bytes.toBytes((Double)obj);
        }
        if(obj instanceof Float){
            return Bytes.toBytes((Float)obj);
        }
        if(obj instanceof Short){
            return Bytes.toBytes((Short)obj);
        }
        return Bytes.toBytes(JSONObject.toJSONString(obj));
    }

    @Override
    public Object byteToValue(byte[] bytes, Type type) {
        if(bytes == null){
            return null;
        }
        if (type.equals(String.class)) {
            return Bytes.toString(bytes);
        }
        if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
            return Bytes.toInt(bytes);
        }
        if (type.equals(Long.class) || type.equals(Long.TYPE)) {
            return Bytes.toLong(bytes);
        }
        if(type.equals(Boolean.class) || type.equals(Boolean.TYPE)){
            return Bytes.toBoolean(bytes);
        }
        if (type.equals(BigDecimal.class)) {
            return Bytes.toBigDecimal(bytes);
        }
        if (type.equals(Double.class) || type.equals(Double.TYPE)) {
            return Bytes.toDouble(bytes);
        }
        if (type.equals(Float.class) || type.equals(Float.TYPE)) {
            return Bytes.toFloat(bytes);
        }
        if (type.equals(Short.class) || type.equals(Short.TYPE)) {
            return Bytes.toShort(bytes);
        }
        //对于Hbase Bytes类不支持的对象类型，统一用json转字符串处理
        return JSONObject.parseObject(Bytes.toString(bytes),type);
    }
}