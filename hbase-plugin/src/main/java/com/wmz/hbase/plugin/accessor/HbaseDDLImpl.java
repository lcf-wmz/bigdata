package com.wmz.hbase.plugin.accessor;

import com.wmz.hbase.plugin.annotation.HbaseFamily;
import com.wmz.hbase.plugin.annotation.HbaseTable;
import com.wmz.hbase.plugin.exception.*;
import com.wmz.hbase.plugin.util.ConvertsUtil;
import com.wmz.hbase.plugin.util.HbasePluginAnnoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.exceptions.HBaseException;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @create: 2020-06-08
 **/
@Slf4j
public abstract class  HbaseDDLImpl implements HbaseDDL{

    @Override
    public <T> boolean createNameSpace(Class<T> clazz) throws IOException {
        checkClass(clazz);
        String nameSpace = getNamespace(clazz);
        if(StringUtils.isBlank(nameSpace)){
            log.warn(clazz.getName() + " not config namespace ,use default namespace");
            return false;
        }
        if(hasNameSpace(nameSpace)){
            log.debug("nameSpace: " + nameSpace + "exist");
            return false;
        }

        NamespaceDescriptor namespaceDescriptor = NamespaceDescriptor.create(nameSpace).build();
        getConnection().getAdmin().createNamespace(namespaceDescriptor);
        log.info("namespace：" + nameSpace + " created");
        return true;
    }

    public <T> boolean hasNameSpace(String nameSpace) throws IOException {
        NamespaceDescriptor[] namespaceDescriptors = getNamespaceDescriptor();
        for(NamespaceDescriptor nd : namespaceDescriptors){
            if(nameSpace.equals(nd.getName())){
                return true;
            }
        }
        return false;
    }

    private String getNamespace(Class<?> clazz){
        HbaseTable hbaseTable = HbasePluginAnnoUtil.getHbaseTable(clazz);
        String tableNameStr = hbaseTable.table();
        int index = tableNameStr.indexOf(":");
        if(index <= 0){
            return "";
        }
        String nameSpace = tableNameStr.substring(0,index);
        return nameSpace;
    }


    /*创建表*/  /*过期时间，分区数 可配置*/
    @Override
    public <T> boolean createTable(Class<T> clazz) throws IOException, HBaseException {
        if (hasTable(clazz)) {
            log.warn("table is exist");
            return false;
        }
        TableDescriptor tableDescriptor = getTableDescriptor(clazz);
        createNameSpace(clazz);
        byte[][] splitKeys = getSplitKeys(clazz);
        if(splitKeys == null){
            getConnection().getAdmin().createTable(tableDescriptor);
        }else{
            getConnection().getAdmin().createTable(tableDescriptor,splitKeys);
        }
        log.info(HbasePluginAnnoUtil.getHbaseTable(clazz).table() + " created");
        return true;
    }

    private volatile  boolean  isNamespaceCache = false;
    private NamespaceDescriptor[] namespaceDescriptorsCache;

    private NamespaceDescriptor[] getNamespaceDescriptor() throws IOException {
        if(!isNamespaceCache){
            synchronized (this) {
                if (isNamespaceCache) {
                    return namespaceDescriptorsCache;
                }
                namespaceDescriptorsCache = getConnection().getAdmin().listNamespaceDescriptors();
                isNamespaceCache = true;
            }
        }
        return namespaceDescriptorsCache;
    }



    private <T>  byte[][] getSplitKeys(Class<T> clazz){
        HbaseTable hbaseTable = HbasePluginAnnoUtil.getHbaseTable(clazz);
        String [] regionSplitKeys = hbaseTable.regionSplitKeys();
        int regionNum = hbaseTable.regionNum();
        byte[][] splitKeys = getSplitKeys(regionSplitKeys,regionNum);
        return splitKeys;
    }

    private <T>  TableDescriptor getTableDescriptor(Class<T> clazz) throws HBaseException {
        HbaseTable hbaseTable = HbasePluginAnnoUtil.getHbaseTable(clazz);
        String tableNameStr = hbaseTable.table();
        TableName tableName = TableName.valueOf(tableNameStr);
        TableDescriptorBuilder builder = TableDescriptorBuilder.newBuilder(tableName);
        Collection<HbaseFamily> familyAnnos = HbasePluginAnnoUtil.getFamilyNames(clazz);
        Collection<ColumnFamilyDescriptor> families = new ArrayList<>();
        for(HbaseFamily familyAnno : familyAnnos){
            families.add(ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(familyAnno.family()))
                    .setTimeToLive(familyAnno.expire())
                    .setCompressionType(familyAnno.compressionType())
                    .build());
        }
        builder.setColumnFamilies(families);
        return builder.build();
    }

    /*检测是否有相应的表*/
    public boolean hasTable(Class clazz) throws IOException {
        checkClass(clazz);
        String tableNameStr = HbasePluginAnnoUtil.getTableName(clazz);
        Admin admin = getConnection().getAdmin();
        TableName tableName = TableName.valueOf(tableNameStr);
        if (admin.tableExists(tableName)) {
            return true;
        } else {
            return false;
        }
    }

    protected Connection getConnection() throws IOException {
        return ConnectionFactory.createConnection();
    }

    /**
     * 自定义获取分区splitKeys ，根据分区数值不同位数的自动构造splitKeys
     */
    public static byte[][] getSplitKeys(String[] regionSplitKeys,Integer regionNum){
        if(regionSplitKeys == null || regionSplitKeys.length == 0){
            if(regionNum == 1){
                return null;
            }
            DecimalFormat decimalFormat = ConvertsUtil.getRowFormatByRegionNum(regionNum);
            int splitKeysArrSize = regionNum - 1;
            regionSplitKeys = new String[splitKeysArrSize];
            for(int i = 1 ; i <= splitKeysArrSize ; i++){
                regionSplitKeys[i-1] = decimalFormat.format(i);
            }
        }

        return getSplitKeys(regionSplitKeys);
    }



    private static byte[][] getSplitKeys(String[] regionSplitKeys){
        byte[][] splitKeys = new byte[regionSplitKeys.length][];
        //升序排序
        TreeSet<byte[]> rows = new TreeSet<byte[]>(Bytes.BYTES_COMPARATOR);
        for(String key : regionSplitKeys){
            rows.add(Bytes.toBytes(key));
        }

        Iterator<byte[]> rowKeyIter = rows.iterator();
        int i=0;
        while (rowKeyIter.hasNext()) {
            byte[] tempRow = rowKeyIter.next();
            rowKeyIter.remove();
            splitKeys[i] = tempRow;
            i++;
        }
        return splitKeys;
    }


    @Override
    public <T> boolean updateTable(Class<T> clazz) throws IOException, HBaseException {
        if (!hasTable(clazz)) {
            createTable(clazz);
            return true;
        }
        TableName tableName = TableName.valueOf(HbasePluginAnnoUtil.getHbaseTable(clazz).table());
        HbaseTable hbaseTable = HbasePluginAnnoUtil.getHbaseTable(clazz);
        int regionNum = hbaseTable.regionNum();
        List<RegionInfo> regionInfoList = getConnection().getAdmin().getRegions(tableName);
        if(regionNum == regionInfoList.size()){
            TableDescriptor tableDescriptor = getTableDescriptor(clazz);
            Admin admin = getConnection().getAdmin();
            TableDescriptor oldTableDescriptor = admin.getDescriptor(tableName);
            if(!tableDescriptor.equals(oldTableDescriptor)){
                log.info(tableName + " updated");
                admin.modifyTable(tableDescriptor);
            }
        }else{
            deleteTable(clazz);
            createTable(clazz);
        }
        return true;
    }



    /*删除表*/
    @Override
    public boolean deleteTable(Class clazz) throws IOException {
        checkClass(clazz);
        Admin admin = getConnection().getAdmin();
        String tableNameStr = HbasePluginAnnoUtil.getTableName(clazz);
        TableName tableName = TableName.valueOf(tableNameStr);
        if (hasTable(clazz)) {
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
            log.info(tableNameStr + " deleted");
            return true;
        } else {
            log.warn(tableNameStr + " not found");
            return false;
        }
    }

    private static volatile Map<Class,Boolean> checkClassMap = new ConcurrentHashMap<>();

    static <T> void checkClass(Class<T> clazz){
        Boolean isCheck = checkClassMap.get(clazz);
        if(isCheck != null ){
            if(!isCheck){
                log.error(clazz.getName() + " anno check fail");
            }
            return;
        }
        HbaseTable hbaseTable = HbasePluginAnnoUtil.getHbaseTable(clazz);
        if(hbaseTable == null){
            checkClassMap.put(clazz,false);
            throw new ClassNotExistAnnoHbaseTableException(clazz);
        }
        Field field = HbasePluginAnnoUtil.getHbaseRowKey(clazz);
        if(field == null){
            checkClassMap.put(clazz,false);
            throw new ClassNotExistAnnoRowKeyException(clazz);
        }
        Set hbaseFieldSet = HbasePluginAnnoUtil.getHbaseFieldSet(clazz);
        if(CollectionUtils.isEmpty(hbaseFieldSet)){
            checkClassMap.put(clazz,false);
            throw new ClassNotExistAnnoHbaseFieldException(clazz);
        }
        checkClassMap.put(clazz,true);
    }

}
