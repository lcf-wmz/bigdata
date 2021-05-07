package com.wmz.hbase.plugin.support;



import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Locale.ENGLISH;

/**
 * Lambda 解析工具类
 *
 * @author HCL, MieMie
 * @since 2018-05-10
 */
public final class LambdaUtils {

    /**
     * SerializedLambda 反序列化缓存
     */
    private static final Map<String, WeakReference<SerializedLambda>> FUNC_CACHE = new ConcurrentHashMap<>();

    /**
     * 解析 lambda 表达式, 该方法只是调用了 {@link SerializedLambda#resolve(SFunction)} 中的方法，在此基础上加了缓存。
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param func 需要解析的 lambda 对象
     * @param <T>  类型，被调用的 Function 对象的目标类型
     * @return 返回解析后的结果
     * @see SerializedLambda#resolve(SFunction)
     */
    public static <T> SerializedLambda resolve(SFunction<T, ?> func) {
        Class<?> clazz = func.getClass();
        String name = clazz.getName();
        return Optional.ofNullable(FUNC_CACHE.get(name))
                .map(WeakReference::get)
                .orElseGet(() -> {
                    SerializedLambda lambda = SerializedLambda.resolve(func);
                    FUNC_CACHE.put(name, new WeakReference<>(lambda));
                    return lambda;
                });
    }



}
