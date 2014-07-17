package com.keqiaokeji.dborm.core;

import com.keqiaokeji.dborm.annotation.AnnotationUtils;
import com.keqiaokeji.dborm.domain.TableBean;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存放Dborm中的缓存
 *
 * @author KEQIAO KEJI
 * @time 2013-5-6上午10:37:50
 */
public class Cache {


    /**
     * 缓存数据表的所有表结构相关的信息<br>
     * 键为数据表对应的类路径<br>
     * 值为表结构相关信息<br>
     * 静态缓存（程序启动时会一次性初始化完毕）
     */
    private static ConcurrentHashMap<String, TableBean> tablesCache = new ConcurrentHashMap<String, TableBean>();


    public static void putTablesCache(String classPath, TableBean tableBean) {
        tablesCache.put(classPath, tableBean);
    }

    public static void putAllTablesCache(Map<String, TableBean> tables) {
        tablesCache.putAll(tables);
    }

    public static TableBean getTablesCache(Class<?> entityClass) {
        TableBean table = tablesCache.get(entityClass.getName());
        if (table == null) {//如果缓存中不存在则从该类的注解中解析信息，如果解析出信息则添加到缓存中，否则抛出异常
            table = new AnnotationUtils().getTableDomain(entityClass);
            if (table != null) {
                putTablesCache(entityClass.getName(), table);
            } else {
                throw new RuntimeException("无法获得表信息，请使用注解或者xml描述表信息！");
            }
        }
        return table;
    }


    /**
     * 缓存类的所有属性<br>
     * 键为类路径<br>
     * 值为该类的属性集合，值的结构：<br>
     * 键：属性对应的列名（将驼峰形式的属性名转换为下划线分割形式的列名）<br>
     * 值：对应的属性对象<br>
     * 动态缓存（随着程序的运行逐渐新增进来，比如用到某一个对象的时候先从缓存中取，如果存在则直接使用，如果不存在则创建并新增到缓存之后使用）
     */
    private static ConcurrentHashMap<String, Map<String, Field>> entityAllFieldsCache = new ConcurrentHashMap<String, Map<String, Field>>();

    public static void putEntityAllFieldsCache(String classPath, Map<String, Field> allFiles) {
        entityAllFieldsCache.put(classPath, allFiles);
    }

    public static Map<String, Field> getEntityAllFieldsCache(Class<?> entityClass) {
        Map<String, Field> allFields = entityAllFieldsCache.get(entityClass.getName());
        if (allFields == null) {// 如果缓存中不存在该对象的反射信息则需解析
            allFields = EntityResolver.getEntityAllFields(entityClass);
            Cache.putEntityAllFieldsCache(entityClass.getName(), allFields);
        }
        return allFields;
    }

    /**
     * 缓存类的列属性<br>
     * 键为类路径<br>
     * 值为该类的属性集合，值的结构：<br>
     * 键：属性对应的列名（将驼峰形式的属性名转换为下划线分割形式的列名）<br>
     * 值：对应的属性对象<br>
     * 动态初始化
     */
    private static ConcurrentHashMap<String, Map<String, Field>> entityColumnFieldsCache = new ConcurrentHashMap<String, Map<String, Field>>();

    public static void putEntityColumnFieldsCache(String classPath, Map<String, Field> columnFields) {
        entityColumnFieldsCache.put(classPath, columnFields);
    }

    public static Map<String, Field> getEntityColumnFieldsCache(Class<?> entityClass) {
        Map<String, Field> columnFields = entityColumnFieldsCache.get(entityClass.getName());
        if (columnFields == null) {// 如果缓存中不存在该对象的反射信息则需解析
            columnFields = EntityResolver.getEntityColumnFields(entityClass);
            Cache.putEntityColumnFieldsCache(entityClass.getName(), columnFields);
        }
        return columnFields;
    }

    /**
     * 缓存类的主键属性<br>
     * 键为类路径<br>
     * 值为该类的属性集合，值的结构：<br>
     * 键：属性对应的列名（将驼峰形式的属性名转换为下划线分割形式的列名）<br>
     * 值：对应的属性对象<br>
     * 动态缓存
     */
    private static ConcurrentHashMap<String, Map<String, Field>> entityPrimaryKeyFieldsCache = new ConcurrentHashMap<String, Map<String, Field>>();

    public static void putEntityPrimaryKeyFieldsCache(String classPath, Map<String, Field> primaryKeyFiles) {
        entityPrimaryKeyFieldsCache.put(classPath, primaryKeyFiles);
    }

    public static Map<String, Field> getEntityPrimaryKeyFieldsCache(Class<?> entityClass) {
        Map<String, Field> primaryKeys = entityPrimaryKeyFieldsCache.get(entityClass.getName());
        if (primaryKeys == null) {// 如果缓存中不存在该对象的反射信息则需解析
            primaryKeys = EntityResolver.getEntityPrimaryKeyFields(entityClass);
            Cache.putEntityPrimaryKeyFieldsCache(entityClass.getName(), primaryKeys);
        }
        return primaryKeys;
    }

    /**
     * 缓存面向对象的SQL语句，如对整个对象的新增、修改及删除的SQL语句，所需参数用?代替<br>
     * 键：类名+操作符（如com.dborm.Login.DELETE：对Login对象的删除语句、com.dborm.Login.UPDATE：
     * 对Login对象的修改语句）<br>
     * 值：SQL语句<br>
     * 动态缓存
     */
    private static ConcurrentHashMap<String, String> sqlCache = new ConcurrentHashMap<String, String>();

    public static void putSqlCache(String classPathSql, String sql) {
        sqlCache.put(classPathSql, sql);
    }

    public static String getSqlCache(String classPathSql) {
        return sqlCache.get(classPathSql);
    }

    public static ConcurrentHashMap<String, TableBean> getTablesCache() {
        return tablesCache;
    }

    public static void setTablesCache(ConcurrentHashMap<String, TableBean> tablesCache) {
        Cache.tablesCache = tablesCache;
    }

    public static ConcurrentHashMap<String, Map<String, Field>> getEntityAllFieldsCache() {
        return entityAllFieldsCache;
    }

    public static void setEntityAllFieldsCache(ConcurrentHashMap<String, Map<String, Field>> entityAllFieldsCache) {
        Cache.entityAllFieldsCache = entityAllFieldsCache;
    }

    public static ConcurrentHashMap<String, Map<String, Field>> getEntityColumnFieldsCache() {
        return entityColumnFieldsCache;
    }

    public static void setEntityColumnFieldsCache(ConcurrentHashMap<String, Map<String, Field>> entityColumnFieldsCache) {
        Cache.entityColumnFieldsCache = entityColumnFieldsCache;
    }

    public static ConcurrentHashMap<String, Map<String, Field>> getEntityPrimaryKeyFieldsCache() {
        return entityPrimaryKeyFieldsCache;
    }

    public static void setEntityPrimaryKeyFieldsCache(ConcurrentHashMap<String, Map<String, Field>> entityPrimaryKeyFieldsCache) {
        Cache.entityPrimaryKeyFieldsCache = entityPrimaryKeyFieldsCache;
    }

    public static ConcurrentHashMap<String, String> getSqlCache() {
        return sqlCache;
    }

    public static void setSqlCache(ConcurrentHashMap<String, String> sqlCache) {
        Cache.sqlCache = sqlCache;
    }
}
