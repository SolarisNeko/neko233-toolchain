package com.neko233.common.reflect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author SolarisNeko
 * @date 2022-02-22
 */
public class ReflectUtil {

    public static Object getValueByField(Object data, String fieldName) {
        Object value;
        Field field;
        Class<?> aClass = data.getClass();
        try {
            field = aClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            field = null;
        }

        Class<?> superclass = aClass.getSuperclass();
        if (field == null) {
            // 获取最近的 field
            field = getFieldShortly(superclass, fieldName);
        }
        if (field == null) {
            return null;
        }

        field.setAccessible(true);
        try {
            value = field.get(data);
        } catch (IllegalAccessException e) {
            return null;
        }
        return value;
    }

    /**
     * 递归获取 field, 最近的
     *
     * @param clazz     类
     * @param fieldName 字段名
     * @return 最近的 field / null
     */
    private static Field getFieldShortly(Class<?> clazz, String fieldName) {
        Class<?> tempClass = clazz;
        while (!"Object".equals(tempClass.getSimpleName())) {
            Field field;
            try {
                field = tempClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                field = null;
            }
            if (field != null) {
                return field;
            }
            tempClass = tempClass.getSuperclass();
        }
        return null;
    }

    /**
     * 递归获取所有 field
     *
     * @param schema
     * @return
     */
    public static List<Field> getAllFieldsRecursive(Class<?> schema) {
        List<Field> fields = new ArrayList<>();
        Class<?> temp = schema;
        while (!"Object".equals(temp.getSimpleName())) {
            Field[] declaredFields = temp.getDeclaredFields();
            fields.addAll(Arrays.asList(declaredFields));
            temp = temp.getSuperclass();
        }
        return fields;
    }
}