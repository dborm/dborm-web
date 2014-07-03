package com.keqiaokeji.dborm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 级联操作标识，用于深度操作
 *
 * @author KEQIAO KEJI
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface Relation {


}
