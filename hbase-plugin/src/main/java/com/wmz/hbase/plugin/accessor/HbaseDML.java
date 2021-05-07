package com.wmz.hbase.plugin.accessor;

import com.wmz.hbase.plugin.support.SFunction;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.exceptions.HBaseException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface HbaseDML extends HbaseDDL {

    <T> void put(T t) throws IOException;
    <T> void put(T t, Collection<String> columns) throws IOException;
    <T> void put(T t, SFunction<T,?>...columns) throws IOException;
    <T> void putBatch(Collection<T> list) throws IOException;
    <T> void putBatch(Collection<T> list,Collection<String> columns) throws IOException;
    <T> void putBatch(Collection<T> list,SFunction<T,?>...columns) throws IOException;

    <T,R> void delete(R rowkey, Class<T> clazz) throws IOException;
    <T,R> void delete(R rowkey, Class<T> clazz, Collection<String> columns) throws IOException;
    <T,R> void delete(R rowkey, Class<T> clazz, SFunction<T,?>...columns) throws IOException;
    <T,R> void deleteBatch(Collection<R> list, Class<T> clazz) throws IOException;
    <T,R> void deleteBatch(Collection<R> list, Class<T> clazz, Collection<String> columns) throws IOException;
    <T,R> void deleteBatch(Collection<R> list, Class<T> clazz, SFunction<T,?>...columns) throws IOException;

    <T,R> T get(R rowkey, Class<T> clazz) throws IOException, HBaseException;
    <T,R> T get(R rowkey, Class<T> clazz, Collection<String> columns) throws IOException, HBaseException;
    <T,R> T get(R rowkey, Class<T> clazz, SFunction<T,?>...columns) throws IOException, HBaseException;


    <T,R> List<T> getBatch(List<R> rowkey, Class<T> clazz) throws IOException, HBaseException;
    <T,R> List<T> getBatch(List<R> rowkey, Class<T> clazz, Collection<String> columns) throws IOException, HBaseException;
    <T,R> List<T> getBatch(List<R> rowkey, Class<T> clazz, SFunction<T,?>...columns) throws IOException, HBaseException;

    <T> Iterable<T> scan(Scan scan, Class<T> clazz) throws IOException;
    <T> Iterable<T> scan(Object startrowkey, Object stoprowkey, Class<T> clazz) throws IOException;
    <T> Iterable<T> scan(Object startrowkey, Object stoprowkey, Class<T> clazz, Collection<String> columns) throws IOException;
    <T> Iterable<T> scan(Object startrowkey, Object stoprowkey, Class<T> clazz, SFunction<T,?>...columns) throws IOException;

    <T> Iterable<T> scanByRowPrefix(String rowPrefix,Class<T> clazz) throws IOException;
    <T> Iterable<T> scanByRowPrefix(String rowPrefix,Class<T> clazz,Collection<String> columns) throws IOException;
    <T> Iterable<T> scanByRowPrefix(String rowPrefix,Class<T> clazz,SFunction<T,?>...columns) throws IOException;

    <T> boolean hasRow(Object rowkey, Class<T> clazz) throws IOException;


    <T,R> long incrementColumnValue(R rowkey, Class<T> clazz, String column, long amount) throws IOException;
    <T,R> long incrementColumnValue(R rowkey, Class<T> clazz, SFunction<T,?> columns, long amount) throws IOException;

}
