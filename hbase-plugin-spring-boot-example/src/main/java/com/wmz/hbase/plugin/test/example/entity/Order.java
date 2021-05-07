package com.wmz.hbase.plugin.test.example.entity;

import com.wmz.hbase.plugin.annotation.HbaseField;
import com.wmz.hbase.plugin.annotation.HbaseRowKey;
import com.wmz.hbase.plugin.annotation.HbaseTable;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @description: 订单记录（已完成订单）
 * @create: 2020-10-12
 **/
@HbaseTable(table = "test:order",regionNum = 10)
@Data
public class Order implements IOrder{

    //订单id
//    @HbaseRowKey
    private String id;
    //交易时间
    @HbaseField
    private LocalDateTime tranTime;
    //用户id
    @HbaseField
    private String userId;
    //商品id
    @HbaseField
    private String goodsId;
    //商品数量
    @HbaseField
    private long num;
    //商品单价
    @HbaseField
    private double price;
    //支付金额
    @HbaseField
    private double payAmount;
    //优惠金额
    @HbaseField
    private double discountAmount;

}
