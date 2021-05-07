package com.wmz.hbase.plugin.parse;

import com.wmz.hbase.plugin.annotation.HbaseFamily;
import com.wmz.hbase.plugin.annotation.HbaseField;
import com.wmz.hbase.plugin.annotation.HbaseRowKey;
import com.wmz.hbase.plugin.annotation.HbaseTable;
import com.wmz.hbase.plugin.exception.MethodNoMatchedFieldException;
import com.wmz.hbase.plugin.util.ConvertsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @description:
 * @create: 2021-01-08
 **/
@Slf4j
public class AnnoMapperParse {

    private volatile static Map<Class<?>,Metadata> CLASS_METADATA_MAP = new ConcurrentHashMap<>(30);

    public static Metadata getAnnoObjectByClass(Class<?> clazz){
        Metadata metadata = CLASS_METADATA_MAP.get(clazz);
        if(metadata == null){
            synchronized (clazz){
                if(metadata == null) {
                    metadata = new Metadata();
                    metadata.init();
                    CLASS_METADATA_MAP.put(clazz, metadata);
                    Class<?> currentClass = clazz;
                    while (currentClass != null) {
                        initClassAnnoObj(metadata,currentClass);
                        currentClass = currentClass.getSuperclass();
                    }
                }
            }
        }
        try {
            metadata = (Metadata)BeanUtils.cloneBean(metadata);
        } catch (Exception e) {
            log.error("clone metadata has exception.",e);
        }
        return metadata;
    }

    private static void initClassAnnoObj(Metadata metadata, Class<?> currentClass){
        HbaseTable hbaseTable = currentClass.getAnnotation(HbaseTable.class);
        if(hbaseTable != null && metadata.getHbaseTable() == null){
            metadata.setHbaseTable(hbaseTable);
        }
        Field[] fields = currentClass.getDeclaredFields();
        parseField(metadata,fields);
        List<Method> methodList = getMethodsByClass(currentClass);
        parseMethod(metadata,methodList,fields);
    }


    private static void  parseField(Metadata metadata, Field[] fields){
        for(Field field : fields){
            HbaseField hbaseField = field.getAnnotation(HbaseField.class);
            if(hbaseField != null){
                metadata.getHbaseFieldSet().add(field);
                metadata.getFieldMap().put(field,hbaseField);
                HbaseFamily hbaseFamily  = hbaseField.family();
                if(hbaseFamily != null){
                    metadata.getHbaseFamilySet().add(hbaseFamily);
                }
            }
            HbaseRowKey hbaseRowKey = field.getAnnotation(HbaseRowKey.class);
            if(hbaseRowKey != null && metadata.getHbaseRowKeyField() == null){
                metadata.setHbaseRowKeyField(field);
            }
        }
    }
    private static void  parseMethod(Metadata metadata, List<Method> methodList,Field[] fields){
        for(Method method : methodList){
            HbaseField hbaseField = method.getAnnotation(HbaseField.class);
            if(hbaseField != null){
                //根据方法名截取找对应的field
                Field field = getFieldByMethod(method,fields);
                if(field == null){
                    throw new MethodNoMatchedFieldException(method);
                }
                metadata.getHbaseFieldSet().add(field);
                metadata.getFieldMap().put(field,hbaseField);
                HbaseFamily hbaseFamily  = hbaseField.family();
                if(hbaseFamily != null){
                    metadata.getHbaseFamilySet().add(hbaseFamily);
                }
            }
            HbaseRowKey hbaseRowKey = method.getAnnotation(HbaseRowKey.class);
            if(hbaseRowKey != null){
                //根据方法名截取找对应的field
                Field field = getFieldByMethod(method,fields);
                if(field == null){
                    throw new MethodNoMatchedFieldException(method);
                }
                if(metadata.getHbaseRowKeyField() == null){
                    metadata.setHbaseRowKeyField(field);
                }
            }
        }
    }

    private static List<Method> getMethodsByClass(Class currentClass){

        List<Method> methodList = new ArrayList<>();
        Method[] methods = currentClass.getDeclaredMethods();
        if(methods != null){
            for(Method method : methods){
                methodList.add(method);
            }
        }
        Class<?>[] interfaceArr = currentClass.getInterfaces();
        addInterfaceMethod(methodList,interfaceArr);
        return methodList;
    }

    private static void addInterfaceMethod(List<Method> methodList ,Class<?>[] interfaceArr ){
        if(interfaceArr != null){
            for(Class currentInterface: interfaceArr){
                Method [] interfaceMethod = currentInterface.getDeclaredMethods();
                for(Method method : interfaceMethod){
                    methodList.add(method);
                }
                addInterfaceMethod(methodList,currentInterface.getInterfaces());
            }
        }
    }

    private static Field getFieldByMethod(Method method,Field[] fields){
        String fieldName = ConvertsUtil.methodToProperty(method.getName());
        for(Field field : fields){
            if(field.getName().equals(fieldName)){
                return field;
            }
        }
        return null;
    }






}
