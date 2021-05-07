package com.wmz.hbase.plugin.spring.boot.autoconfigure;

import com.wmz.hbase.plugin.configure.ModeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @create: 2020-10-12
 **/
@ConfigurationProperties(prefix = "hbase-plugin")
@Data
public class HbasePluginProperties {

    private String zookeeperQuorum;

    private ModeEnum modeEnum = ModeEnum.DEFAULT;
}
