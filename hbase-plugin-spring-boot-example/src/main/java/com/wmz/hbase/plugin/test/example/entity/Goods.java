package com.wmz.hbase.plugin.test.example.entity;

import com.wmz.hbase.plugin.annotation.HbaseFamily;
import com.wmz.hbase.plugin.annotation.HbaseField;
import com.wmz.hbase.plugin.annotation.HbaseRowKey;
import com.wmz.hbase.plugin.annotation.HbaseTable;

import java.time.LocalDate;

/**
 * @description:
 * @create: 2020-10-12
 **/
@HbaseTable(table = "test:goods",regionSplitKeys = {"g"})
public class Goods {
    //商品id
    @HbaseRowKey
    private String id;
    //商品名称
    @HbaseField(qualifier = "goodsName")
    private String name;
    //商品单价
    @HbaseField
    private double price;
    //图片链接
    @HbaseField
    private String picUrl;
    //上市日期
    @HbaseField
    private LocalDate timeToMarket;
    //商品介绍
    @HbaseField(family = @HbaseFamily(family = "g",expire="90 DAYS") )
    private String produce;
}
