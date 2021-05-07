package com.wmz.hbase.plugin.parse;

import com.wmz.hbase.plugin.annotation.HbaseFamily;
import com.wmz.hbase.plugin.annotation.HbaseField;
import com.wmz.hbase.plugin.annotation.HbaseTable;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @description:
 * @create: 2021-01-08
 **/
@Data
public class Metadata {
    private HbaseTable hbaseTable;
    private Field hbaseRowKeyField;
    private Set<Field> hbaseFieldSet;
    private Set<HbaseFamily> hbaseFamilySet;

    //暂不生效，因反射较慢，后面修改
    private Map<Field, HbaseField> fieldMap;

    public  Metadata init(){
        this.setHbaseFieldSet(new CopyOnWriteArraySet<>());
        this.setHbaseFamilySet(new CopyOnWriteArraySet<>());
        this.setFieldMap(new ConcurrentHashMap<>());
        return this;
    }
}
