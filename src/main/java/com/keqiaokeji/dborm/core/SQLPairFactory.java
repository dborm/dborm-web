package com.keqiaokeji.dborm.core;

import com.keqiaokeji.dborm.domain.TableBean;
import com.keqiaokeji.dborm.util.PairDborm;
import com.keqiaokeji.dborm.util.ReflectUtilsDborm;
import com.keqiaokeji.dborm.util.StringUtilsDborm;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.*;
import java.util.Map.Entry;

/**
 * 解析出SQL语句及对应的参数对
 *
 * @author KEQIAO KEJI
 * @time 2013-6-5下午1:31:57
 */
public class SQLPairFactory {


    public static <T> PairDborm<String, Object[]> insert(T entity) {
        entity = Dborm.getDbormDataBase().beforeInsert(entity);
        Class<?> entityClass = entity.getClass();
        String sql = Cache.getSqlCache(entityClass.getName() + ".INSERT");
        if (StringUtilsDborm.isEmpty(sql)) {// 如果缓存中取不到已解析的SQL
            sql = SQLTranslater.getInsertSql(entityClass);
            Cache.putSqlCache(entityClass.getName() + ".INSERT", sql);
        }
        List<Object> bindArgs = EntityResolver.getColumnFiledValuesUseDefault(entity);
        return PairDborm.create(sql, bindArgs.toArray());
    }

    public static <T> List<PairDborm<String, Object[]>> insertDeep(T entity) {
        List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
        pairList.add(insert(entity));
        pairList.addAll(getRelationPair(entity, PairType.INSERT));
        return pairList;
    }

    public static <T> PairDborm<String, Object[]> update(T entity) {
        entity = Dborm.getDbormDataBase().beforeUpdate(entity);
        Class<?> entityClass = entity.getClass();
        String sql = Cache.getSqlCache(entityClass.getName() + ".UPDATE");
        if (StringUtilsDborm.isEmpty(sql)) {// 如果缓存中取不到已解析的SQL
            sql = SQLTranslater.getUpdateSql(entityClass);
            Cache.putSqlCache(entityClass.getName() + ".UPDATE", sql);
        }
        List<Object> bindArgs = EntityResolver.getColumnFiledValues(entity);
        bindArgs.addAll(EntityResolver.getPrimaryKeyFiledValues(entity));
        return PairDborm.create(sql, bindArgs.toArray());
    }

    public static <T> List<PairDborm<String, Object[]>> updateDeep(T entity) {
        List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
        pairList.add(update(entity));
        pairList.addAll(getRelationPair(entity, PairType.UPDATE));
        return pairList;
    }

    public static <T> PairDborm<String, Object[]> delete(T entity) {
        entity = Dborm.getDbormDataBase().beforeDelete(entity);
        Class<?> entityClass = entity.getClass();
        String sql = Cache.getSqlCache(entityClass.getName() + ".DELETE");
        if (StringUtilsDborm.isEmpty(sql)) {// 如果缓存中取不到已解析的SQL
            sql = SQLTranslater.getDeleteSql(entityClass);
            Cache.putSqlCache(entityClass.getName() + ".DELETE", sql);
        }
        List<Object> bindArgs = EntityResolver.getPrimaryKeyFiledValues(entity);
        return PairDborm.create(sql, bindArgs.toArray());
    }

    public static <T> List<PairDborm<String, Object[]>> deleteDeep(T entity) {
        List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
        pairList.add(delete(entity));
        pairList.addAll(getRelationPair(entity, PairType.DELETE));
        return pairList;
    }

    public static <T> PairDborm<String, Object[]> replace(T entity) {
        entity = Dborm.getDbormDataBase().beforeReplace(entity);
        Class<?> entityClass = entity.getClass();
        StringBuilder sqlContent = new StringBuilder("UPDATE ");
        String tableName = Cache.getTablesCache(entityClass).getTableName();
        sqlContent.append(tableName);
        sqlContent.append(" SET ");
        StringBuilder columnName = new StringBuilder();
        List<Object> bindArgs = new ArrayList<Object>();

        Map<String, Field> columnFields = Cache.getEntityColumnFieldsCache(entityClass);
        Set<Entry<String, Field>> entrySet = columnFields.entrySet();
        if (entrySet.size() > 0) {
            for (Entry<String, Field> entry : entrySet) {
                Field field = entry.getValue();
                Object value = ReflectUtilsDborm.getFieldValue(field, entity);
                if (value != null) {// 如果当前属性的值不是null则修改
                    columnName.append(entry.getKey());
                    columnName.append("=?, ");
                    value = DataTypeConverter.fieldValueToColumnValue(value);
                    bindArgs.add(value);
                }
            }
        }
        sqlContent.append(StringUtilsDborm.cutLastSign(columnName.toString(), ", "));
        sqlContent.append(" WHERE ");
        sqlContent.append(SQLTranslater.parsePrimaryKeyWhere(entityClass));
        bindArgs.addAll(EntityResolver.getPrimaryKeyFiledValues(entity));
        return PairDborm.create(sqlContent.toString(), bindArgs.toArray());
    }

    public static <T> List<PairDborm<String, Object[]>> replaceDeep(T entity) {
        List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
        pairList.add(replace(entity));
        pairList.addAll(getRelationPair(entity, PairType.REPLACE));
        return pairList;
    }

    public static <T> List<PairDborm<String, Object[]>> saveOrUpdateDeep(T entity, Connection conn) {
        List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
        if (Dborm.isExist(entity, conn)) {
            pairList.add(update(entity));
        } else {
            pairList.add(insert(entity));
        }
        pairList.addAll(getRelationSavePair(entity, PairType.SAVEORUPDATE, conn));
        return pairList;
    }

    public static <T> List<PairDborm<String, Object[]>> saveOrReplaceDeep(T entity, Connection conn) {
        List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
        if (Dborm.isExist(entity, conn)) {
            pairList.add(replace(entity));
        } else {
            pairList.add(insert(entity));
        }
        pairList.addAll(getRelationSavePair(entity, PairType.SAVEORREPLACE, conn));
        return pairList;
    }

    public static PairDborm<String, Object[]> getEntityCount(Class<?> entityClass) {
        // 例如： SELECT COUNT(*) FROM
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ");
        String tableName = Cache.getTablesCache(entityClass).getTableName();
        sql.append(tableName);
        return PairDborm.create(sql.toString(), null);
    }

    public static <T> PairDborm<String, Object[]> getCountByPrimaryKey(T entity) {
        // 例如： SELECT COUNT(*) FROM users WHERE user_id=?
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ");
        Class<?> entityClass = entity.getClass();
        String tableName = Cache.getTablesCache(entityClass).getTableName();
        sql.append(tableName);
        sql.append(" WHERE ");
        sql.append(SQLTranslater.parsePrimaryKeyWhere(entityClass));
        List<Object> primaryKeyValue = EntityResolver.getPrimaryKeyFiledValues(entity);
        Object[] bindArgs = new String[primaryKeyValue.size()];
        for (int i = 0; i < primaryKeyValue.size(); i++) {
            bindArgs[i] = primaryKeyValue.get(i);
        }
        return PairDborm.create(sql.toString(), bindArgs);
    }

    public static <T> PairDborm<String, Object[]> getEntitiesByExample(T entity, boolean isAnd) {
        Class<?> entityClass = entity.getClass();
        StringBuilder sqlContent = new StringBuilder("SELECT * FROM ");
        String tableName = Cache.getTablesCache(entityClass).getTableName();
        sqlContent.append(tableName);
        sqlContent.append(" WHERE 1=1 ");
        StringBuilder columnName = new StringBuilder();
        List<Object> bindArgs = new ArrayList<Object>();

        Map<String, Field> columnFields = Cache.getEntityColumnFieldsCache(entityClass);
        Set<Entry<String, Field>> entrySet = columnFields.entrySet();
        if (entrySet.size() > 0) {
            for (Entry<String, Field> entry : entrySet) {
                Field field = entry.getValue();
                Object value = ReflectUtilsDborm.getFieldValue(field, entity);
                if (value != null) {// 如果当前属性的值不是null则修改
                    if (isAnd) {
                        columnName.append(" AND ");
                    } else {
                        columnName.append(" OR ");
                    }
                    columnName.append(entry.getKey());
                    columnName.append("=? ");
                    value = DataTypeConverter.fieldValueToColumnValue(value);
                    bindArgs.add(value);
                }
            }
        }
        sqlContent.append(StringUtilsDborm.cutLastSign(columnName.toString(), ", "));
        return PairDborm.create(sqlContent.toString(), bindArgs.toArray());
    }


    /**
     * 当前的级联操作类型
     */
    private static enum PairType {
        INSERT, UPDATE, DELETE, REPLACE, SAVEORUPDATE, SAVEORREPLACE
    }

    /**
     * 获取级联对象的SQL语句对
     *
     * @param entity 对象
     * @param type   操作类型
     * @return SQL操作集合
     * @author KEQIAO KEJI
     * @time 2013-6-5下午1:55:14
     */
    private static <T> List<PairDborm<String, Object[]>> getRelationPair(T entity, PairType type) {
        List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
        Class<?> entityClass = entity.getClass();
        TableBean table = Cache.getTablesCache(entityClass);
        Set<String> relations = table.getRelation();
        if (relations.size() > 0) {
            for (String fieldName : relations) {
                Field relationField = ReflectUtilsDborm.getFieldByName(entityClass, fieldName);
                List<?> relationObjList = (List<?>) ReflectUtilsDborm.getFieldValue(relationField, entity);
                if (relationObjList == null) {
                    continue;
                }
                for (Object relationObj : relationObjList) {
                    switch (type) {
                        case INSERT:
                            pairList.addAll(insertDeep(relationObj));
                            break;
                        case UPDATE:
                            pairList.addAll(updateDeep(relationObj));
                            break;
                        case DELETE:
                            pairList.addAll(deleteDeep(relationObj));
                            break;
                        case REPLACE:
                            pairList.addAll(replaceDeep(relationObj));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return pairList;
    }

    /**
     * 获取级联对象SAVE相关的SQL语句对
     *
     * @param entity 对象
     * @param type   操作类型
     * @param conn   数据库连接
     * @return SQL操作集合
     * @author KEQIAO KEJI
     * @time 2013-6-5下午1:55:14
     */
    private static <T> List<PairDborm<String, Object[]>> getRelationSavePair(T entity, PairType type, Connection conn) {
        List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
        Class<?> entityClass = entity.getClass();
        TableBean table = Cache.getTablesCache(entityClass);
        Set<String> relations = table.getRelation();
        if (relations.size() > 0) {
            for (String fieldName : relations) {
                Field relationField = ReflectUtilsDborm.getFieldByName(entityClass, fieldName);
                List<?> relationObjList = (List<?>) ReflectUtilsDborm.getFieldValue(relationField, entity);
                if (relationObjList == null) {
                    continue;
                }
                for (Object relationObj : relationObjList) {
                    switch (type) {
                        case INSERT:
                            pairList.addAll(insertDeep(relationObj));
                            break;
                        case UPDATE:
                            pairList.addAll(updateDeep(relationObj));
                            break;
                        case DELETE:
                            pairList.addAll(deleteDeep(relationObj));
                            break;
                        case REPLACE:
                            pairList.addAll(replaceDeep(relationObj));
                            break;
                        case SAVEORUPDATE:
                            pairList.addAll(saveOrUpdateDeep(relationObj, conn));
                            break;
                        case SAVEORREPLACE:
                            pairList.addAll(saveOrReplaceDeep(relationObj, conn));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return pairList;
    }


}
