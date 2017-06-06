/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tbc.paas.mdl.cfg.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Ztian
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

	String columnName();

	String columnType() default "";

	boolean isMonitor() default false;
}
