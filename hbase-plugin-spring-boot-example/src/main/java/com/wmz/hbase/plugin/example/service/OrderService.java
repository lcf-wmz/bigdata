package com.wmz.hbase.plugin.example.service;

import com.wmz.hbase.plugin.accessor.HbaseAccessor;
import com.wmz.hbase.plugin.example.entity.Order;
import com.wmz.hbase.plugin.example.entity.OrderSon;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @description:
 * @create: 2020-10-12
 **/
@Service
public class OrderService  {

    @Resource
    private HbaseAccessor hbaseAccessor;


    public void test() throws IOException {
        Order order =  new Order();
        String id = UUID.randomUUID().toString().replace("-","");
        order.setId(id);
        order.setNum(15);
        order.setPrice(10);
        order.setTranTime(LocalDateTime.now());
        //增、改
        hbaseAccessor.put(order);
        //查询所有列值
        Order queryOrder = hbaseAccessor.get(id,Order.class);
        //查询指定列值
        queryOrder = hbaseAccessor.get(id,Order.class,Order::getNum);
        //删除所有列记录
        hbaseAccessor.delete(id,Order.class);
        //删除整定列记录
        hbaseAccessor.delete(id,Order.class,Order::getNum);
        System.out.println(queryOrder);
    }

}
