package cn.cocho.dborm.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 反射工具类
 *
 * @author KEQIAO KEJI
 * @time 2013-5-6上午11:57:27
 */
public class ReflectUtilsDborm {

    /**
     * 获得实体类的所有属性（该方法递归的获取当前类及父类中声明的字段。最终结果以list形式返回）
     *
     * @param entityClass 实体类
     * @return List形式的结果。
     * @author KEQIAO KEJI
     * @time 2013-5-2下午8:53:29
     */
    public static List<Field> getFields(Class<?> entityClass) {
        if (entityClass == null) {
            return null;
        }

        List<Field> fields = new ArrayList<Field>();
        Field[] classFields = entityClass.getDeclaredFields();
        fields.addAll(Arrays.asList(classFields));

        Class<?> superclass = entityClass.getSuperclass();
        if (superclass != null) {
            List<Field> superClassFields = getFields(superclass);
            fields.addAll(superClassFields);
        }
        return fields;
    }

    /**
     * * 通过名称查找一个实体类的字段属性
     *
     * @param entityClass 需要查找的对象。
     * @param fieldName   需要查找的属性字段名称
     * @return 该类对应名称的属性或者null，如果没有该名称的属性。
     * @author KEQIAO KEJI
     * @time 2013-5-6上午11:44:49
     */
    public static Field getFieldByName(Class<?> entityClass, String fieldName) {
        List<Field> fields = ReflectUtilsDborm.getFields(entityClass);
        for (Field field : fields) {
            String name = field.getName();
            if (name.equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    /**
     * 通过属性对象和实体对象获取字段的值
     *
     * @param field  属性对象
     * @param entity 实体对象
     * @return 属性值
     * @author KEQIAO KEJI
     * @time 2013-5-6上午11:45:43
     */
    public static Object getFieldValue(Field field, Object entity) {
        if (field == null || entity == null) {
            return null;
        }

        try {
            // 反射的对象在使用时应该取消 Java 语言访问检查,（用作于反射字段时对字段作用域不检查
            // 例如访问private类型和protected类型的字段）
            field.setAccessible(true);
            return field.get(entity);// 获取字段的值
        } catch (Exception e) {
            LoggerUtilsDborm.error("Can't get field (" + field.getName() + ") value from object (" + entity + ") by reflect!", e);
        }
        return null;
    }

    /**
     * 将值保存到实体对象的指定属性中
     *
     * @param field  属性
     * @param entity 实体对象
     * @param value  值
     * @return true：保存成功，false：保存失败
     * @author KEQIAO KEJI
     * @time 2013-5-6上午11:46:54
     */
    public static boolean setFieldValue(Field field, Object entity, Object value) {
        if (field == null || entity == null) {
            return false;
        }

        try {
            field.setAccessible(true);
            field.set(entity, value);
            return true;
        } catch (Exception e) {
            LoggerUtilsDborm.error("Can't set value（" + value + "） to instance（" + entity.getClass().getName() + "） field（" + field.getName() + "）  by reflect!", e);
        }
        return false;
    }

    /**
     * 根据实体类创建实体对象
     *
     * @param entityClass 实体类
     * @return 实体对象
     * @author KEQIAO KEJI
     * @time 2013-5-6上午11:48:07
     */
    public static Object createInstance(Class<?> entityClass) {
        if (entityClass == null) {
            throw new IllegalArgumentException("Object class mustn't be null");
        }

        try {
            return entityClass.newInstance();
        } catch (Exception e) {
            LoggerUtilsDborm.error("Can't create instance（" + entityClass.getName() + "）  by reflect!", e);
        }
        return null;
    }

}
