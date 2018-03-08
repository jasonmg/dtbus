package com.hawker.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Memoizer 对函数本身，闭包缓存
 * https://opencredo.com/lambda-memoization-in-java-8/
 */
public final class Memoizer {


    /**
     * 对参数f的递归调用不安全
     * <p>
     * Guava MapMaker?
     */
    public static <I, O> Function<I, O> memoize(Function<I, O> f) {
        ConcurrentMap<I, O> lookup = new ConcurrentHashMap<>();
        return input -> lookup.computeIfAbsent(input, f);
    }


    /**
     * 对参数f的递归调用安全
     */
    public static <I, O> Function<I, O> memoize_safe(Function<I, O> f) {
        Map<I, O> lookup = new HashMap<>();
        ReentrantLock lock = new ReentrantLock();
        return input -> {
            lock.lock();
            try {
                return lookup.computeIfAbsent(input, f);
            } finally {
                lock.unlock();
            }
        };
    }
}
