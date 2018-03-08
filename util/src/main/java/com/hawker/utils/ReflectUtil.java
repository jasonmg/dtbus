package com.hawker.utils;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mingjiang.ji on 2017/11/23
 */
public class ReflectUtil {


    public static <T> Map<String, Object> instance2Map(T o) {

        Map<String, Object> res = new HashMap<>();
        Class clz = o.getClass();

        try {
            Field[] privateFields = clz.getDeclaredFields();

            for (Field f : privateFields) {
                String name = StringUtils.capitalize(f.getName());

                try {
                    Method m = clz.getMethod("get" + name);
                    Object a = m.invoke(o);

                    res.put(f.getName(), a);
                } catch (NoSuchMethodException nse) {
                }

            }

        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | SecurityException e) {
            throw new RuntimeException(e.getCause());
        }

        return res;
    }
}
