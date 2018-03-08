package com.hawker.utils;

import com.typesafe.config.Config;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * typesafe config
 * @author mingjiang.ji on 2017/6/12
 */
public class Config4JUtil {


    @SuppressWarnings("unchecked")
    public static <T> T toInstance(Config config, Class<T> clz) {

        Object t;

        try {
            Constructor[] constructors = clz.getDeclaredConstructors();
            Constructor c = constructors[0];

            t = c.newInstance();

            Field[] privateFields = clz.getDeclaredFields();

            for (Field f : privateFields) {
                String name = StringUtils.capitalize(f.getName());

                Object a = build(config, f);
                Method m = clz.getMethod("set" + name, f.getType());
                m.invoke(t, a);
            }

        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e.getCause());
        }

        return (T) t;
    }

    private static Object build(Config c, Field f) {
        Class<?> clz = f.getType();
        String name = f.getName();

        String typeName = clz.getTypeName();

        switch (typeName) {
            case "java.lang.String":
                return c.getString(name);

            // clz.isPrimitive() || ClassUtils.isPrimitiveOrWrapper(clz)
            case "int":
            case "java.lang.Integer":
                return c.getInt(name);
            case "boolean":
            case "java.lang.Boolean":
                return c.getBoolean(name);
            case "float":
            case "double":
            case "java.lang.Float":
            case "java.lang.Double":
                return c.getDouble(name);
            case "long":
            case "java.lang.Long":
                return c.getLong(name);
            default:
        }

        if (clz.isMemberClass()) {

            Config cc = c.getConfig(name);
            return toInstance(cc, clz);

        } else if (clz.isArray() || Collection.class.isAssignableFrom(clz)) {  // array or list

            ParameterizedType tf = (ParameterizedType) f.getGenericType();
            Type[] at = tf.getActualTypeArguments();
            Class typeArgClass = (Class) at[0];

            return buildList(c, name, typeArgClass);
        } else {
            throw new RuntimeException("unsupported class property type: " + name);
        }
    }


    private static Object buildList(Config c, String name, Class typeArgClass) {

        String typeArgName = typeArgClass.getName();


        switch (typeArgName) {
            case "java.lang.String":
                return c.getStringList(name);
            case "java.lang.Integer":
                return c.getIntList(name);
            case "java.lang.Boolean":
                return c.getBooleanList(name);
            case "java.lang.Double":
                return c.getDoubleList(name);
            case "java.lang.Long":
                return c.getLongList(name);
            default:
        }

        if (typeArgClass.isMemberClass()) {

            return c.getConfigList(name)
                    .stream()
                    .map(cd -> toInstance(cd, typeArgClass))
                    .collect(Collectors.toList());

        } else {
            return new RuntimeException("unsupported array/list arg type: " + typeArgName);
        }
    }

}
