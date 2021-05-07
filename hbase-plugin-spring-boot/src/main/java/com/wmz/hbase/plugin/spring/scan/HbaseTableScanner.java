package com.wmz.hbase.plugin.spring.scan;

import com.wmz.hbase.plugin.accessor.HbaseAccessor;
import com.wmz.hbase.plugin.annotation.HbaseTable;
import com.wmz.hbase.plugin.configure.ModeEnum;
import com.wmz.hbase.plugin.spring.boot.autoconfigure.HbasePluginProperties;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

/**
 * @description:
 * @create: 2020-10-13
 **/
@Slf4j
@Setter
public class HbaseTableScanner {

    @Autowired
    private HbasePluginProperties properties;
    @Autowired
    private HbaseAccessor hbaseAccessor;

    private List<String> basePackages;

    private Class<? extends Annotation> annotationClass;

    @PostConstruct
    public void init(){
        //根据模式来设定是否扫描
        ModeEnum modeEnum = properties.getModeEnum();
        boolean forceUpdateTable = false;
        switch (modeEnum){
            case DEFAULT:
                return ;
            case DEV:
                forceUpdateTable = true;
                break;
            case PRO:
                break;
        }
        if(CollectionUtils.isEmpty(basePackages)){
            log.error("@HTableScan  basePackages is empty");
            return;
        }
        log.info("scan Hbase entity start,basePackages is " + basePackages);
        Set<Class> setClass = ClassScanner.scan(basePackages.toArray(new String[basePackages.size()]), annotationClass == null ? HbaseTable.class : annotationClass);
        autoUpdateHbaseTable(setClass,forceUpdateTable);
        log.info("scan Hbase entity end");
    }

    /**
     * 自动更新库中hbase表结构
     * @param classSet @HbaseTable注解过的类集合
     * @param forceUpdate 是否强制更新
     */
    private void autoUpdateHbaseTable(Set<Class> classSet,boolean forceUpdate){
        classSet.parallelStream().forEach(clazz->{
            try{
                if(forceUpdate){
                    hbaseAccessor.updateTable(clazz);
                }else{
                    boolean hasTable = hbaseAccessor.hasTable(clazz);
                    if(!hasTable){
                        hbaseAccessor.createTable(clazz);
                    }
                }
            } catch (Exception e) {
                log.error("autoUpdateHbaseTable has exception:class is " + clazz + ",forceUpdate is " + forceUpdate,e);
            }
        });
    }
}
