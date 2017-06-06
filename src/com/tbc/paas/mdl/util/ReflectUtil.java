/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tbc.paas.mdl.util;

import java.lang.reflect.Field;
import java.util.*;

import org.apache.log4j.Logger;

import com.tbc.paas.mdl.domain.MdlException;

/**
 * @author Ztian
 */
public final class ReflectUtil {
    public static Logger LOG = Logger.getLogger(ReflectUtil.class);

    private ReflectUtil() {
        // NP
    }

    /**
     * 通过字段对象和实体对象获取字段的值
     *
     * @param field 字段
     * @param obj   实体对象
     * @return 字段的值
     */
    public static Object getFieldValue(Field field, Object obj) {
        if (field == null || obj == null) {
            throw new IllegalArgumentException("Can get field (" + field
                    + ") from object (" + obj + ")!");
        }

        try {
            // 反射的对象在使用时应该取消 Java 语言访问检查,（用作于反射字段时对字段作用域不检查 例如访问
            // private类型和protected类型的字段）
            field.setAccessible(true);
            // 获取字段的值
            return field.get(obj);
        } catch (Exception e) {
            LOG.error("Can't get value by reflect!", e);
        }

        return null;
    }

    /**
     * 该方法主要是把一些数据库的类型适配成合适的Java类型。目前只适用于支持枚举。
     */
    public static Object adapterValue(Field field, Object value) {
        Class<?> fieldType = field.getType();
        if (fieldType.isEnum()) {
            value = Enum.valueOf((Class<? extends Enum>) fieldType, value.toString());
        }

        return value;
    }

    /**
     * 将值保存到实体类的字段中
     *
     * @param field 字段
     * @param obj   实体类
     * @param value 值
     * @return 成功标志
     */
    public static boolean setFieldValue(Field field, Object obj, Object value) {
        if (field == null || obj == null) {
            throw new IllegalArgumentException("Can get field (" + field
                    + ") from object (" + obj + ")!");
        }

        value = adapterValue(field, value);

        try {
            field.setAccessible(true);
            field.set(obj, value);
            return true;
        } catch (Exception e) {
            LOG.error("Can't set value by reflect!", e);
        }

        return false;
    }

    public static Object createInstance(Class<?> objectClass) {

        if (objectClass == null) {
            throw new IllegalArgumentException("Object class mustn't be null");
        }

        try {
            return objectClass.newInstance();
        } catch (Exception e) {
            LOG.error("Can't create instance  by reflect!", e);
        }

        return null;
    }

    public static <T> T newInstance(Class<T> objectClass) {

        if (objectClass == null) {
            throw new IllegalArgumentException("Object class mustn't be null");
        }

        try {
            return objectClass.newInstance();
        } catch (Exception e) {
            throw new MdlException("Can't create instance  by reflect!", e);
        }
    }

    /**
     * 找到继承链中所有类声明的属性，如果父类和子类有同名的属性，子类的属性优先获取。
     *
     * @param clazz 要分析获取的当前类
     * @return 所有类声明的属性
     */
    public static Map<String, Field> getClassFields(Class<?> clazz) {
        Class<?> tempClazz = clazz;
        List<Class<?>> clazzChain = new ArrayList<Class<?>>();
        while (tempClazz != null && tempClazz != Object.class) {
            clazzChain.add(tempClazz);
            tempClazz = tempClazz.getSuperclass();
        }

        Map<String, Field> fieldMap = new HashMap<String, Field>();
        for (int i = clazzChain.size() - 1; i >= 0; i--) {
            tempClazz = clazzChain.get(i);
            Field[] declaredFields = tempClazz.getDeclaredFields();
            for (Field field : declaredFields) {
                fieldMap.put(field.getName(), field);
            }
        }

        return fieldMap;
    }
}