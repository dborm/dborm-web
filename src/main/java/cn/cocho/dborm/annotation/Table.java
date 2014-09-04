package cn.cocho.dborm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表的注解
 *
 * @author KEQIAO KEJI
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface Table {

    /**
     * 表的名称<br>
     * 如果不填写，则将驼峰格式的类名转换为下划线格式的名称作为表明<br>
     * 如LoginUser表明为login_user
     *
     * @author KEQIAO KEJI
     * @time 2013-5-2下午4:08:21
     */
    public String tableName() default "";

}
