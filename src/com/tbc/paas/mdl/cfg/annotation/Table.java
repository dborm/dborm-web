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
@Target(value = { ElementType.TYPE })
public @interface Table {

	String shared() default "";

	String schema() default "";

	String tableName();
}
