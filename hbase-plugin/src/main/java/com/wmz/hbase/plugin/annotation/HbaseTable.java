package com.wmz.hbase.plugin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HbaseTable {
    //表名
    String table() ;

    /**
     * 分区间隔key值数组：
     * 按照regionSplitKeys划分分区
     * （如果配置regionSplitKeys，那么regionNum配置将失效）
     */
    String[] regionSplitKeys() default {};

    /**
     * 预分区的分区数：
     * 配置regionNum,按数值进行分区数划分
     * （默认分区规则为：
     *     分区数为n,
     *     10个及以下，regionSplitKeys = 1|2|3|...n...|7|8|9
     *     10个以上100个及以下，regionSplitKeys = 01|02|03|...n...|97|98|99
     *     100个以上1000个以下。。。
     *
     *    rowKey计算方法默认为：
     *
     *   1.根据@HbaseRowKey注解规则的得到唯一确定该记录的值code
     *   2.rowKey = Mod(hash(code),regionNum) + code
     *
     *   @HbaseRowKey注解规则查看
     *   @see HbaseRowKey#order()
     *
     *  ）
     */
    int regionNum() default 1;

}
