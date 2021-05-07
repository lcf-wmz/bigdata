package com.wmz.hbase.plugin.accessor;

import org.apache.hadoop.hbase.exceptions.HBaseException;

import java.io.IOException;
import java.util.List;

/**
 * @description:
 * @create: 2020-06-08
 **/
public interface HbaseDDL {

    /**
     * 根据clazz的hbase表结构配置，更新表结构
     * @param clazz
     * @param <T>
     * @return
     * @throws IOException
     * @throws HBaseException
     */
    <T>boolean updateTable(Class<T> clazz) throws IOException, HBaseException;

    <T>boolean createNameSpace(Class<T> clazz) throws IOException;

    <T>boolean createTable(Class<T> clazz) throws IOException, HBaseException;

    <T>boolean deleteTable(Class<T> clazz)  throws IOException ;


}
