package com.wmz.hbase.plugin.support;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @description:
 * @create: 2021-01-08
 **/
@FunctionalInterface
public interface SFunction<T, R> extends Function<T, R>, Serializable {
}