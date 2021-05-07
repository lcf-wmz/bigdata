package com.wmz.hbase.plugin.accessor;

import com.wmz.hbase.plugin.parse.convert.ColumnConverter;
import com.wmz.hbase.plugin.parse.convert.Converter;
import com.wmz.hbase.plugin.parse.convert.RowKeyConverter;
import com.wmz.hbase.plugin.support.LambdaUtils;
import com.wmz.hbase.plugin.support.SFunction;
import com.wmz.hbase.plugin.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.exceptions.HBaseException;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;

import javax.ws.rs.NotSupportedException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
public class HbaseAccessor extends HbaseDDLImpl implements HbaseDML {

    private static Connection connection;

    private Configuration configuration;

    public HbaseAccessor(Configuration configuration) throws IOException {
        this.configuration = configuration;
        if(configuration == null){
            connection = ConnectionFactory.createConnection();
        }else{
            connection = ConnectionFactory.createConnection(configuration);
        }
    }

    @Override
    protected Connection getConnection() {
        return connection;
    }

    /*添加数据*/
    @Override
    public void put(Object t) throws IOException {
        put(t,(Collection<String>)null);
    }


    @Override
    public <T> void put(T t, Collection<String> columns) throws IOException {
        if(t == null){
            return ;
        }
        Class clazz = t.getClass();
        checkClass(clazz);
        String tableNameStr = HbasePluginAnnoUtil.getTableName(clazz);
        Table table = connection.getTable(TableName.valueOf(tableNameStr));
        Put put = consturctPut(t,clazz,columns);
        table.put(put);
    }

    @Override
    public <T> void put(T t, SFunction<T, ?>... columns) throws IOException {
        put(t,lambdaParse(columns));
    }

    private <T> Collection<String> lambdaParse(SFunction<T, ?>... columns){
        Set set = Collections.EMPTY_SET;
        if (ArrayUtils.isNotEmpty(columns)) {
            set = Arrays.stream(columns).map(i -> lambdaParse(i)).collect(Collectors.toSet());
        }
        return set;
    }

    private <T> String lambdaParse(SFunction<T, ?> column){
        return ConvertsUtil.methodToProperty(LambdaUtils.resolve(column).getImplMethodName());
    }


    private <T> Put consturctPut(T t,Class<T> clazz,Collection<String> columns){
        Object keyValue = HbasePluginAnnoUtil.getRowKeyValue(t);
        Put put = new Put(ConvertsUtil.rowKeyConvertToBytes(keyValue,clazz));
        addPutCell(put, t,columns);
        return put;
    }

    /*批量添加数据*/
    @Override
    public <T> void putBatch(Collection<T> collection) throws IOException {
        putBatch(collection,(Collection<String>)null);
    }

    @Override
    public <T> void putBatch(Collection<T> list, Collection<String> columns) throws IOException {
        if(CollectionUtils.isEmpty(list)){
            log.warn("putBatch:collection is empty");
            return;
        }
        T t = list.iterator().next();
        Class clazz = t.getClass();
        checkClass(clazz);
        Table table = getTableByClass(clazz);
        Stream<Put> stream = list.stream().map(c->consturctPut(c,clazz,columns));
        List<Put> putList  = stream.collect(Collectors.toList());
        table.put(putList);
    }

    @Override
    public <T> void putBatch(Collection<T> list, SFunction<T, ?>... columns) throws IOException {
        putBatch(list,lambdaParse(columns));
    }

    /*根据rowkey获得具体的model对象*/
    @Override
    public <T,R> T get(R rowkey, Class<T> clazz) throws IOException {
        return get(rowkey,clazz,(Collection<String>)null);
    }


    /*根据rowkey获得指定列，并将结果转换为具体的model对象*/
    @Override
    public  <T,R> T get(R rowkey, Class<T> clazz, Collection<String> columnCollection) throws IOException {
        if(rowkey == null){
            return null;
        }
        checkClass(clazz);
        Get get = new Get(ConvertsUtil.rowKeyConvertToBytes(rowkey,clazz));
        Set<Field> fields = HbasePluginAnnoUtil.getHbaseFieldSet(clazz,columnCollection);
        fields.forEach(field->{
            String family = HbasePluginAnnoUtil.getFamily(field);
            String column = HbasePluginAnnoUtil.getColumn(field);
            get.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
        });
        Table table = getTableByClass(clazz);
        Result result = table.get(get);
        if (result.isEmpty()) return null;
        return ConvertsUtil.resultToValue(result,clazz);
    }

    @Override
    public <T, R> T get(R rowkey, Class<T> clazz, SFunction<T, ?>... columns) throws IOException {
        return get(rowkey,clazz,lambdaParse(columns));
    }

    @Override
    public <T, R> List<T> getBatch(List<R> rowkeyList, Class<T> clazz) throws IOException {
        return getBatch(rowkeyList,clazz,(Collection<String>)null);
    }

    @Override
    public <T, R> List<T> getBatch(List<R> rowkeyList, Class<T> clazz, Collection<String> columns) throws IOException {
        if(CollectionUtils.isEmpty(rowkeyList)){
            return Collections.emptyList();
        }
        checkClass(clazz);
        Set<Field> fields = HbasePluginAnnoUtil.getHbaseFieldSet(clazz,columns);
        List<Get> getList = rowkeyList.stream().map(rowkey-> {
            Get get = new Get(ConvertsUtil.rowKeyConvertToBytes(rowkey,clazz));
            fields.forEach(field->{
                String family = HbasePluginAnnoUtil.getFamily(field);
                String column = HbasePluginAnnoUtil.getColumn(field);
                get.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
            });
            return get;
        }).collect(Collectors.toList());

        Table table = getTableByClass(clazz);
        Result[] resultArr = table.get(getList);
        if (ArrayUtils.isEmpty(resultArr)) {
            return Collections.emptyList();
        }
        List<T> resultList = new ArrayList<>();
        for(Result result : resultArr){
            if (!result.isEmpty()){
                T t = ConvertsUtil.resultToValue(result,clazz);
                resultList.add(t);
            }
        }
        return resultList;
    }

    @Override
    public <T, R> List<T> getBatch(List<R> rowkey, Class<T> clazz, SFunction<T, ?>... columns) throws IOException, HBaseException {
        return getBatch(rowkey,clazz,lambdaParse(columns));
    }



    /*根据起始位置扫描表，并返回model的list集合*/
    @Override
    public <T> Iterable scan(Object startrowkey, Object stoprowkey, Class<T> clazz) throws IOException {
        return scan(startrowkey, stoprowkey, clazz,(Collection<String>)null);
    }

    /*根据起始位置扫描表，获取指定列，并返回model的list集合*/
    @Override
    public <T> Iterable<T> scan(Object startrowkey, Object stoprowkey, Class<T> clazz, Collection<String> columnCollection) throws IOException {
        checkClass(clazz);
        Scan scan = new Scan();
        if(startrowkey != null){
            scan.withStartRow(ConvertsUtil.rowKeyConvertToBytes(startrowkey,clazz));
        }
        if(stoprowkey != null){
            scan.withStopRow(ConvertsUtil.rowKeyConvertToBytes(stoprowkey,clazz));
        }
        Set<Field> fields = HbasePluginAnnoUtil.getHbaseFieldSet(clazz,columnCollection);
        fields.forEach(field->{
            String family = HbasePluginAnnoUtil.getFamily(field);
            String column = HbasePluginAnnoUtil.getColumn(field);
            scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
        });
        return scan(scan,clazz);
    }

    @Override
    public <T> Iterable<T> scan(Object startrowkey, Object stoprowkey, Class<T> clazz, SFunction<T, ?>... columns) throws IOException {
        return scan(startrowkey, stoprowkey, clazz,lambdaParse(columns));
    }

    @Override
    public <T> Iterable<T> scanByRowPrefix(String rowPrefix, Class<T> clazz) throws IOException {
        return scanByRowPrefix(rowPrefix,clazz,(Collection<String>)null);
    }

    @Override
    public <T> Iterable<T> scanByRowPrefix(String rowPrefix, Class<T> clazz, Collection<String> columns) throws IOException {
        //根据是否预分区 去判断 自动预分区 还是手动配置的
        boolean  isAutoRegion = HbasePluginAnnoUtil.isAutoRegion(clazz);
        Scan scan = new Scan();
        Set<Field> fields = HbasePluginAnnoUtil.getHbaseFieldSet(clazz,columns);
        fields.forEach(field->{
            String family = HbasePluginAnnoUtil.getFamily(field);
            String column = HbasePluginAnnoUtil.getColumn(field);
            scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
        });
        if(isAutoRegion){
            //自动分区
            int regionNum = HbasePluginAnnoUtil.getRegionNum(clazz);
            int preStrSize = ConvertsUtil.getPreStrSize(regionNum);
            scan.setFilter(new RowFilter(CompareOperator.EQUAL,new RegexStringComparator("^\\d{" + preStrSize +"}" + rowPrefix + ".*")));
        }else{
            //手动配置分区
            scan.setRowPrefixFilter(Bytes.toBytes(rowPrefix));
        }
        return scan(scan,clazz);
    }

    @Override
    public <T> Iterable<T> scanByRowPrefix(String rowPrefix, Class<T> clazz, SFunction<T, ?>... columns) throws IOException {
        return scanByRowPrefix(rowPrefix,clazz,lambdaParse(columns));
    }

    @Override
    public <T, R> long incrementColumnValue(R rowkey, Class<T> clazz, String column, long amount) throws IOException {
        checkClass(clazz);
        Table table = getTableByClass(clazz);
        Set<Field> fields = HbasePluginAnnoUtil.getHbaseFieldSet(clazz,Arrays.asList(column));
        if(CollectionUtils.isEmpty(fields)){
            log.error("clazz {} property {} not anno @HbaseField",clazz,column);
            return 0;
        }
        Field field = fields.iterator().next();
        Type type = field.getGenericType();
        if(!Converter.isHbaseSuppertIncrementType(type)){
            throw new NotSupportedException("type " + type + "not supported increment");
        }
        String family = HbasePluginAnnoUtil.getFamily(field);
        String hcolumn = HbasePluginAnnoUtil.getColumn(field);
        byte[] rowBytes = ConvertsUtil.rowKeyConvertToBytes(rowkey,clazz);
        return table.incrementColumnValue(rowBytes,Bytes.toBytes(family),Bytes.toBytes(hcolumn),amount);
    }

    @Override
    public <T, R> long incrementColumnValue(R rowkey, Class<T> clazz, SFunction<T, ?> column, long amount) throws IOException {
        return incrementColumnValue(rowkey,clazz,lambdaParse(column),amount);
    }

    public <T> Iterable<T> scan(Scan scan,Class<T> clazz) throws IOException {
        Table table = getTableByClass(clazz);
        ResultScanner scanner = table.getScanner(scan);
        return ConvertsUtil.resultScanToValue(scanner,clazz);
    }

    /*根据rowkey删除指定行*/
    @Override
    public  <T,R> void delete(R rowkey, Class<T> clazz) throws IOException {
        delete(rowkey,clazz,(Collection<String>)null);
    }

    @Override
    public <T, R> void delete(R rowkey, Class<T> clazz, Collection<String> columns) throws IOException {
        checkClass(clazz);
        Table table = getTableByClass(clazz);
        Delete delete = new Delete(ConvertsUtil.rowKeyConvertToBytes(rowkey,clazz));
        Set<Field> fields = HbasePluginAnnoUtil.getHbaseFieldSet(clazz,columns);
        fields.forEach(field->{
            String family = HbasePluginAnnoUtil.getFamily(field);
            String column = HbasePluginAnnoUtil.getColumn(field);
            delete.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
        });
        table.delete(delete);
    }

    @Override
    public <T, R> void delete(R rowkey, Class<T> clazz, SFunction<T, ?>... columns) throws IOException {
        delete(rowkey, clazz, lambdaParse(columns));
    }

    @Override
    public <T,R> void deleteBatch(Collection<R> collection, Class<T> clazz)  throws IOException {
        deleteBatch(collection,clazz,(Collection<String>)null);
    }

    @Override
    public <T, R> void deleteBatch(Collection<R> list, Class<T> clazz, Collection<String> columns) throws IOException {
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        checkClass(clazz);
        Table table = getTableByClass(clazz);
        Set<Field> fields = HbasePluginAnnoUtil.getHbaseFieldSet(clazz,columns);
        List<Delete> deleteList = list.stream().map(c-> {
            Delete delete = new Delete(ConvertsUtil.rowKeyConvertToBytes(c,clazz));
            fields.forEach(field->{
                String family = HbasePluginAnnoUtil.getFamily(field);
                String column = HbasePluginAnnoUtil.getColumn(field);
                delete.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
            });
            return delete;
        }).collect(Collectors.toList());
        table.delete(deleteList);
    }

    @Override
    public <T, R> void deleteBatch(Collection<R> list, Class<T> clazz, SFunction<T, ?>... columns) throws IOException {
        deleteBatch(list,clazz,lambdaParse(columns));
    }

    /*根据rowkey判断是否含有某条数据*/
    @Override
    public boolean hasRow(Object rowkey, Class clazz) throws IOException {
        if(rowkey == null){
            return false;
        }
        checkClass(clazz);
        Table table = getTableByClass(clazz);
        Get get = new Get(ConvertsUtil.rowKeyConvertToBytes(rowkey,clazz));
        Field field = HbasePluginAnnoUtil.getHbaseFieldSet(clazz).iterator().next();
        String family = HbasePluginAnnoUtil.getFamily(field);
        String column = HbasePluginAnnoUtil.getColumn(field);
        get.addColumn(Bytes.toBytes(family),Bytes.toBytes(column));
        return table.exists(get);
    }



    private static void addPutCell(Put put, Object model,Collection<String> columns) {
        Class<?> clazz = model.getClass();
        Set<Field> fields = HbasePluginAnnoUtil.getHbaseFieldSet(clazz,columns);
        fields.forEach(field -> {
            Object value = HbasePluginAnnoUtil.getFieldValueByField(model, field);
            if(value == null) return;
            String family  = HbasePluginAnnoUtil.getFamily(field);
            String column = HbasePluginAnnoUtil.getColumn(field);
            put.addColumn(Bytes.toBytes(family), Bytes.toBytes(column), ColumnConverter.getInstance().valueToByte(value));
        });
    }


    private static Table getTableByClass(Class clazz) throws IOException {
        String tableName = HbasePluginAnnoUtil.getTableName(clazz);
        Table table = connection.getTable(TableName.valueOf(tableName));
        return table;
    }


}
