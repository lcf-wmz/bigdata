package com.wmz.hbase.plugin.test.example.service;

import com.wmz.hbase.plugin.accessor.HbaseAccessor;
import com.wmz.hbase.plugin.test.example.entity.Order;
import com.wmz.hbase.plugin.test.example.entity.OrderSon;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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
        Order order =  new OrderSon();
        String id = UUID.randomUUID().toString().replace("-","");
        order.setId(id);
        order.setNum(15);
        order.setPrice(10);
        order.setTranTime(LocalDateTime.now());
        hbaseAccessor.put(order);
        System.out.println(hbaseAccessor.get(id,Order.class,Order::getNum));
        hbaseAccessor.incrementColumnValue(id,Order.class,Order::getNum,1);

        Order queryOrder = hbaseAccessor.get(id,Order.class);
        System.out.println(queryOrder);
    }

}
