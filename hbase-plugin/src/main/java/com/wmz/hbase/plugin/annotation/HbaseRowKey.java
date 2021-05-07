package com.wmz.hbase.plugin.annotation;


import java.lang.annotation.*;

/**
 * Created by caesar.zhu on 15-9-22.
 */
@Target({ElementType.FIELD,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited()
public @interface HbaseRowKey {
    /**
     * 对于多个属性合并为一条才能确定唯一一条记录的情况
     *  @HbaseRowKey注解多个字段  （暂不支持）
     * 用order标明拼接的顺序，用来在进行rowKey计算的时候，按order顺序拼接之后的字符串进行hash计算
     * @return
     */
//    int order() default 1;
}
