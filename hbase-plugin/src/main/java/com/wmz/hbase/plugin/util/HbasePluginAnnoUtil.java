package com.wmz.hbase.plugin.util;

import com.wmz.hbase.plugin.accessor.HbaseAccessor;
import com.wmz.hbase.plugin.annotation.HbaseFamily;
import com.wmz.hbase.plugin.annotation.HbaseField;
import com.wmz.hbase.plugin.annotation.HbaseTable;
import com.wmz.hbase.plugin.exception.*;
import com.wmz.hbase.plugin.parse.AnnoMapperParse;
import com.wmz.hbase.plugin.parse.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class HbasePluginAnnoUtil {

    /*获取表名*/
    public static String getTableName(Class<?> clazz){
        return getHbaseTable(clazz).table();
    }

    public static String[] getRegionSplitKeys(Class<?> clazz){
        return getHbaseTable(clazz).regionSplitKeys();
    }

    public static int getRegionNum(Class<?> clazz){
        return getHbaseTable(clazz).regionNum();
    }



    /*获取表名注解对象*/
    public static HbaseTable getHbaseTable(Class<?> clazz){
        Metadata metadata = AnnoMapperParse.getAnnoObjectByClass(clazz);
        HbaseTable hbaseTable = metadata.getHbaseTable();
        if (hbaseTable != null) {
            return hbaseTable;
        }
        throw new ClassNotExistAnnoHbaseTableException(clazz);
    }


    public static Field getHbaseRowKey(Class<?> clazz){
        Metadata metadata = AnnoMapperParse.getAnnoObjectByClass(clazz);
        Field hbaseRowKeyField =  metadata.getHbaseRowKeyField();
        if(hbaseRowKeyField != null){
            return hbaseRowKeyField;
        }
        throw new ClassNotExistAnnoRowKeyException(clazz);
    }


    public static Set<Field> getHbaseFieldSet(Class<?> clazz){
        Metadata metadata = AnnoMapperParse.getAnnoObjectByClass(clazz);
        Set<Field> hbaseFieldSet =  metadata.getHbaseFieldSet();
        if(CollectionUtils.isNotEmpty(hbaseFieldSet)){
            return hbaseFieldSet;
        }
        throw new ClassNotExistAnnoHbaseFieldException(clazz);
    }

    public static Set<Field> getHbaseFieldSet(Class<?> clazz,Collection<String> columns){
        return getHbaseFieldSet(clazz).stream().filter(new ColumnFilter(columns)).collect(Collectors.toSet());
    }

    /*获取列簇名*/
    public static Set<HbaseFamily> getFamilyNames(Class<?> clazz){
        Metadata metadata = AnnoMapperParse.getAnnoObjectByClass(clazz);
        return metadata.getHbaseFamilySet();
    }


    /*获得model的HbaseRowKey字段名*/
    public static String getRowKeyName(Class clazz) {
        return HbasePluginAnnoUtil.getHbaseRowKey(clazz).getName();
    }

    /*获得model的HbaseRowKey的值*/
    public static <T> Object getRowKeyValue(T model){
        return getFieldValueByField(model,HbasePluginAnnoUtil.getHbaseRowKey(model.getClass()));
    }


    public static String getFamily(Field field){
        HbaseField hbaseField = field.getDeclaredAnnotation(HbaseField.class);
        if(hbaseField == null){
            throw new FieldNotUserHbaseFieldAnnoException(field);
        }
        return hbaseField.family().family();
    }

    public static String getColumn(Field field){
        HbaseField hbaseField = field.getDeclaredAnnotation(HbaseField.class);
        if(hbaseField == null){
            throw new FieldNotUserHbaseFieldAnnoException(field);
        }
        String qualifier = hbaseField.qualifier();
        return StringUtils.isNotBlank(qualifier) ? qualifier : field.getName();
    }


    public static boolean isAutoRegion(Class<?> clazz){
        int regionNum = HbasePluginAnnoUtil.getRegionNum(clazz);
        String[] regionSplitKeys = HbasePluginAnnoUtil.getRegionSplitKeys(clazz);
        if(regionNum <= 1 || ArrayUtils.isNotEmpty(regionSplitKeys)){
            return false;
        }
        return true;
    }

    public static <T> Object getFieldValueByField(T model, Field field) {
        Object o = null;
        try{
            field.setAccessible(true);
            o = field.get(model);
        }catch (Exception e){
            log.error("get value has ex. class is " + model.getClass() + " ,filed is" + field,e);
        }
        return o;
    }
    public static <T,V> void  setFieldValueByField(T model, Field field,V value) {
        try {
            field.setAccessible(true);
            field.set(model,value);
        } catch (Exception e) {
            log.error("set Value fail,filed is "+ field +",value="+value + ",class is " + model.getClass(),e);
        }
    }

    private static class ColumnFilter implements Predicate<Field> {
        private Collection<String> columnCollection;
        private boolean isFilter;

        public ColumnFilter(Collection<String> columnCollection) {
            this.columnCollection = columnCollection;
            this.isFilter = CollectionUtils.isEmpty(columnCollection);
        }
        @Override
        public boolean test(Field field) {
            String column = HbasePluginAnnoUtil.getColumn(field);
            return isFilter || columnCollection.contains(column);
        }
    }


}
