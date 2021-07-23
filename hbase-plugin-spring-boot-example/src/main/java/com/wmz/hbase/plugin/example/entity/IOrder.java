package com.wmz.hbase.plugin.example.entity;

import com.wmz.hbase.plugin.annotation.HbaseRowKey;

/**
 * @description:
 * @create: 2021-01-04
 **/
public interface IOrder {
    @HbaseRowKey
    String getId();
}
