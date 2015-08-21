package cn.cocho.dborm.core;

import cn.cocho.dborm.annotation.AnnotationUtils;
import cn.cocho.dborm.domain.TableBean;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存放Dborm中的缓存
 *
 * @author KEQIAO KEJI
 * @time 2013-5-6上午10:37:50
 */
public class CacheDborm {


    private static CacheDborm cache;
    EntityResolver entityResolver = new EntityResolver();

    public static synchronized CacheDborm getCache() {
        if (cache == null) {
            cache = new CacheDborm();
        }
        return cache;
    }

    /**
     * 缓存数据表的所有表结构相关的信息<br>
     * 键为数据表对应的类路径<br>
     * 值为表结构相关信息<br>
     * 静态缓存（程序启动时会一次性初始化完毕）
     */
    private ConcurrentHashMap<String, TableBean> tablesCache = new ConcurrentHashMap<String, TableBean>();


    public void putTablesCache(String classPath, TableBean tableBean) {
        tablesCache.put(classPath, tableBean);
    }

    public void putAllTablesCache(Map<String, TableBean> tables) {
        tablesCache.putAll(tables);
    }

    public TableBean getTablesCache(Class<?> entityClass) {
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
    private ConcurrentHashMap<String, Map<String, Field>> entityAllFieldsCache = new ConcurrentHashMap<String, Map<String, Field>>();

    public void putEntityAllFieldsCache(String classPath, Map<String, Field> allFiles) {
        entityAllFieldsCache.put(classPath, allFiles);
    }

    public Map<String, Field> getEntityAllFieldsCache(Class<?> entityClass) {
        Map<String, Field> allFields = entityAllFieldsCache.get(entityClass.getName());
        if (allFields == null) {// 如果缓存中不存在该对象的反射信息则需解析
            allFields = entityResolver.getEntityAllFields(entityClass);
            putEntityAllFieldsCache(entityClass.getName(), allFields);
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
    private ConcurrentHashMap<String, Map<String, Field>> entityColumnFieldsCache = new ConcurrentHashMap<String, Map<String, Field>>();

    public void putEntityColumnFieldsCache(String classPath, Map<String, Field> columnFields) {
        entityColumnFieldsCache.put(classPath, columnFields);
    }

    public Map<String, Field> getEntityColumnFieldsCache(Class<?> entityClass) {
        Map<String, Field> columnFields = entityColumnFieldsCache.get(entityClass.getName());
        if (columnFields == null) {// 如果缓存中不存在该对象的反射信息则需解析
            columnFields = entityResolver.getEntityColumnFields(entityClass);
            putEntityColumnFieldsCache(entityClass.getName(), columnFields);
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
    private ConcurrentHashMap<String, Map<String, Field>> entityPrimaryKeyFieldsCache = new ConcurrentHashMap<String, Map<String, Field>>();

    public void putEntityPrimaryKeyFieldsCache(String classPath, Map<String, Field> primaryKeyFiles) {
        entityPrimaryKeyFieldsCache.put(classPath, primaryKeyFiles);
    }

    public Map<String, Field> getEntityPrimaryKeyFieldsCache(Class<?> entityClass) {
        Map<String, Field> primaryKeys = entityPrimaryKeyFieldsCache.get(entityClass.getName());
        if (primaryKeys == null) {// 如果缓存中不存在该对象的反射信息则需解析
            primaryKeys = entityResolver.getEntityPrimaryKeyFields(entityClass);
            putEntityPrimaryKeyFieldsCache(entityClass.getName(), primaryKeys);
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
    private ConcurrentHashMap<String, String> sqlCache = new ConcurrentHashMap<String, String>();

    public void putSqlCache(String classPathSql, String sql) {
        sqlCache.put(classPathSql, sql);
    }

    public String getSqlCache(String classPathSql) {
        return sqlCache.get(classPathSql);
    }

    public ConcurrentHashMap<String, TableBean> getTablesCache() {
        return tablesCache;
    }

    public void setTablesCache(ConcurrentHashMap<String, TableBean> tablesCache) {
        tablesCache = tablesCache;
    }

    public ConcurrentHashMap<String, Map<String, Field>> getEntityAllFieldsCache() {
        return entityAllFieldsCache;
    }

    public void setEntityAllFieldsCache(ConcurrentHashMap<String, Map<String, Field>> entityAllFieldsCache) {
        entityAllFieldsCache = entityAllFieldsCache;
    }

    public ConcurrentHashMap<String, Map<String, Field>> getEntityColumnFieldsCache() {
        return entityColumnFieldsCache;
    }

    public void setEntityColumnFieldsCache(ConcurrentHashMap<String, Map<String, Field>> entityColumnFieldsCache) {
        entityColumnFieldsCache = entityColumnFieldsCache;
    }

    public ConcurrentHashMap<String, Map<String, Field>> getEntityPrimaryKeyFieldsCache() {
        return entityPrimaryKeyFieldsCache;
    }

    public void setEntityPrimaryKeyFieldsCache(ConcurrentHashMap<String, Map<String, Field>> entityPrimaryKeyFieldsCache) {
        entityPrimaryKeyFieldsCache = entityPrimaryKeyFieldsCache;
    }

    public ConcurrentHashMap<String, String> getSqlCache() {
        return sqlCache;
    }

    public void setSqlCache(ConcurrentHashMap<String, String> sqlCache) {
        sqlCache = sqlCache;
    }
}
