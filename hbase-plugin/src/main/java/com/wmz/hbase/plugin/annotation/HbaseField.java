package com.wmz.hbase.plugin.annotation;


import java.lang.annotation.*;


@Target({ElementType.FIELD,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited()
public @interface HbaseField {

    HbaseFamily family() default @HbaseFamily;

    String qualifier() default "";


}
