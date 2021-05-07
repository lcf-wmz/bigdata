package com.wmz.hbase.plugin.util;



import com.google.common.collect.Iterables;
import com.wmz.hbase.plugin.exception.ParseHbaseFieldAnnoMethodException;
import com.wmz.hbase.plugin.parse.convert.ColumnConverter;
import com.wmz.hbase.plugin.parse.convert.Converter;
import com.wmz.hbase.plugin.parse.convert.RowKeyConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.*;

@Slf4j
public class ConvertsUtil {


    /*将Hbase返回的Result对象解析为具体的model对象*/
    public static <T> Iterable<T> resultScanToValue(ResultScanner resultScanner, Class<T> clazz) {
        return Iterables.transform(resultScanner,result-> resultToValue(result,clazz));
    }

    public static <T> T resultToValue(Result result, Class<T> clazz) {
        T model = null;
        try {
            model = clazz.newInstance();
        } catch (Exception e) {
            log.error("init obj fail,class is " + clazz,e);
        }
        if(model == null){
            return null;
        }
        byte[] rowBytes = result.getRow();
        Object rowValue = bytesConvertToRowKey(rowBytes, clazz);
        HbasePluginAnnoUtil.setFieldValueByField(model,HbasePluginAnnoUtil.getHbaseRowKey(clazz),rowValue);
        Set<Field> fields = HbasePluginAnnoUtil.getHbaseFieldSet(clazz);
        for (Field field : fields) {
            String family = HbasePluginAnnoUtil.getFamily(field);
            String column = HbasePluginAnnoUtil.getColumn(field);
            byte[] bytes = result.getValue(Bytes.toBytes(family), Bytes.toBytes(column));
            Type type = field.getGenericType();
            Object value = ColumnConverter.getInstance().byteToValue(bytes,type);
            if(value == null){
                continue;
            }
            HbasePluginAnnoUtil.setFieldValueByField(model,field,value);
        }
        return model;
    }



    private static boolean isIterableClass(Class clazz){
        return isBelongInterfaceClass(clazz,Iterable.class);
    }

    private static boolean isMapClass(Class clazz){
        return isBelongInterfaceClass(clazz, Map.class);
    }

    private static boolean isBelongInterfaceClass(Class clazz,Class pClazz){
        if(clazz.equals(pClazz)){
            return true;
        }
        Class [] clazzArr = clazz.getInterfaces();
        if(ArrayUtils.isNotEmpty(clazzArr)){
            for(Class c : clazzArr){
                if(isBelongInterfaceClass(c,pClazz)){
                    return true;
                }
            }
        }
        return false;
    }




    /**
     * 将@HbaseRowKey注解的Obj对象转换为rowkey字节数组
     * @param obj  @HbaseRowKey注解的Obj对象
     * @param clazz @HbaseRowKey注解的Obj对象 所属的@HbaseTable注解的类
     * @return rowkey字节数组
     */
    public  static <T> byte[] rowKeyConvertToBytes(T obj,Class<?> clazz) {
        if(obj == null){
            throw new RuntimeException("class " +  clazz.getName() + " rowKey is null");
        }
        boolean  isAutoRegion = HbasePluginAnnoUtil.isAutoRegion(clazz);
        if(!isAutoRegion){
            //已配置好分区
            return RowKeyConverter.getInstance().valueToByte(obj);
        }
        //自动根据分区数预分区
        String objStr = Converter.convertToString(obj);
        int regionNum = HbasePluginAnnoUtil.getRegionNum(clazz);
        String rowKey = getRowKey(objStr,regionNum);
        return Bytes.toBytes(rowKey);
    }

    /**
     * 将rowkey字节数组转换为@HbaseRowKey注解的Obj对象
     * @param bytes rowkey字节数组
     * @param clazz @HbaseRowKey注解的Obj对象 所属的@HbaseTable注解的类
     * @return Object  @HbaseRowKey注解的Obj对象
     */
    public  static Object  bytesConvertToRowKey(byte[] bytes,Class<?> clazz) {
        if(bytes == null){
            throw new RuntimeException("bytes is null");
        }
        Field rowKeyField =  HbasePluginAnnoUtil.getHbaseRowKey(clazz);
        Object rowValue = RowKeyConverter.getInstance().byteToValue(bytes,rowKeyField.getGenericType());
        boolean  isAutoRegion = HbasePluginAnnoUtil.isAutoRegion(clazz);
        if(!isAutoRegion){
            //已配置好分区
            return rowValue;
        }
        //自动根据分区数预分区
        int regionNum = HbasePluginAnnoUtil.getRegionNum(clazz);
        int size = getPreStrSize(regionNum);
        String row = rowValue.toString().substring(size);
        return row;
    }


    /**
     * 自动分区rowKey
     * @param code
     * @param regionNum
     * @return
     */
    public static String getRowKey(String code, Integer regionNum) {
        int regionCode = code.hashCode() % regionNum;
        if(regionCode < 0) {
            regionCode = regionCode * -1;
        }
        DecimalFormat decimalFormat = getRowFormatByRegionNum(regionNum);
        return decimalFormat.format(regionCode) + code;
    }

    public static DecimalFormat getRowFormatByRegionNum(Integer regionNum){
        String pattern = "";
        for(int i = 0 ; i < getPreStrSize(regionNum) ; i++){
            pattern = pattern + "0";
        }
        return new DecimalFormat(pattern);
    }

    public static int getPreStrSize(Integer regionNum){
        regionNum --;
        int size = (regionNum + "").length();
        return size;
    }


    public static String methodToProperty(String name ) {
        if (name.startsWith("is")) {
            name = name.substring(2);
        } else {
            if (!name.startsWith("get") && !name.startsWith("set")) {
                throw new ParseHbaseFieldAnnoMethodException(name);
            }
            name = name.substring(3);
        }
        if (name.length() == 1 || name.length() > 1 && !Character.isUpperCase(name.charAt(1))) {
            name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }

        return name;
    }

}
