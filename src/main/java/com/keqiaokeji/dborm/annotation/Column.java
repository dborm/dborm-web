package com.keqiaokeji.dborm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 列的注解
 *
 * @author KEQIAO KEJI
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Column {

    /**
     * 是否作为主键
     */
    public boolean isPrimaryKey() default false;

    /**
     * 默认值(字符串"null"或者null相当于没有默认值)
     *
     * @author KEQIAO KEJI
     */
    public String defaultValue() default "null";

}
