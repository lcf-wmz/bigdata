package com.wmz.hbase.plugin.example;

import com.wmz.hbase.plugin.example.service.OrderService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description:
 * @create: 2021-05-07
 **/
@Component
public class CustomInitializer implements ApplicationRunner {

    @Resource
    private OrderService orderService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        orderService.test();
    }
}
