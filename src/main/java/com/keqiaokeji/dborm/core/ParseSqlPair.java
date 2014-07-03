package com.keqiaokeji.dborm.core;

import com.keqiaokeji.dborm.domain.TableBean;
import com.keqiaokeji.dborm.schema.DbormSchemaScan;
import com.keqiaokeji.dborm.util.LogDborm;
import com.keqiaokeji.dborm.util.PairDborm;
import com.keqiaokeji.dborm.util.ReflectUtilsDborm;
import com.keqiaokeji.dborm.util.StringUtilsDborm;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 解析出SQL语句及对应的参数对
 *
 * @author KEQIAO KEJI
 * @time 2013-6-5下午1:31:57
 */
class ParseSqlPair {

    private ParseEntity parseEntity;
    private ParseSql parseSql;
    private ConvertDBType convertDBType;
    private DbormConnectionDB connectionDB;

    public ParseSqlPair() {
        parseEntity = new ParseEntity();
        parseSql = new ParseSql();
        convertDBType = new ConvertDBType();
        connectionDB = new DbormConnectionDB();
    }

    public <T> PairDborm<String, Object[]> insert(T entity) {
        entity = Dborm.getDbormDataBase().beforeInsert(entity);
        Class<?> entityClass = entity.getClass();
        String sql = Cache.getSqlCache(entityClass.getName() + ".INSERT");
        if (StringUtilsDborm.isEmpty(sql)) {// 如果缓存中取不到已解析的SQL
            sql = parseSql.getInsertSql(entityClass);
        }
        List<Object> bindArgs = parseEntity.getColumnFiledValuesUseDefault(entity);
        return PairDborm.create(sql, bindArgs.toArray());
    }

    public <T> List<PairDborm<String, Object[]>> insertDeep(T entity) {
        List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
        pairList.add(insert(entity));
        pairList.addAll(getRelationFieldPair(entity, PairType.INSERT));
        return pairList;
    }

    public <T> PairDborm<String, Object[]> update(T entity) {
        entity = Dborm.getDbormDataBase().beforeUpdate(entity);
        Class<?> entityClass = entity.getClass();
        String sql = Cache.getSqlCache(entityClass.getName() + ".UPDATE");
        if (StringUtilsDborm.isEmpty(sql)) {// 如果缓存中取不到已解析的SQL
            sql = parseSql.getUpdateSql(entityClass);
        }
        List<Object> bindArgs = parseEntity.getColumnFiledValuesUseDefault(entity);
        bindArgs.addAll(parseEntity.getPrimaryKeyFiledValues(entity));
        return PairDborm.create(sql, bindArgs.toArray());
    }

    public <T> List<PairDborm<String, Object[]>> updateDeep(T entity) {
        List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
        pairList.add(update(entity));
        pairList.addAll(getRelationFieldPair(entity, PairType.UPDATE));
        return pairList;
    }

    public <T> PairDborm<String, Object[]> delete(T entity) {
        entity = Dborm.getDbormDataBase().beforeDelete(entity);
        Class<?> entityClass = entity.getClass();
        String sql = Cache.getSqlCache(entityClass.getName() + ".DELETE");
        if (StringUtilsDborm.isEmpty(sql)) {// 如果缓存中取不到已解析的SQL
            sql = parseSql.getDeleteSql(entityClass);
        }
        List<Object> bindArgs = parseEntity.getPrimaryKeyFiledValues(entity);
        return PairDborm.create(sql, bindArgs.toArray());
    }

    public <T> List<PairDborm<String, Object[]>> deleteDeep(T entity) {
        List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
        pairList.add(delete(entity));
        pairList.addAll(getRelationFieldPair(entity, PairType.DELETE));
        return pairList;
    }

    public <T> PairDborm<String, Object[]> replace(T entity) {
        Class<?> entityClass = entity.getClass();
        StringBuilder sqlContent = new StringBuilder("UPDATE ");
        String tableName = DbormSchemaScan.getTableDomain(entityClass.getName()).getTableName();
        sqlContent.append(tableName);
        sqlContent.append(" SET ");
        StringBuilder columnName = new StringBuilder();
        List<Object> bindArgs = new ArrayList<Object>();

        Map<String, Field> columnFields = parseEntity.getEntityColumnFields(entityClass);
        Set<Entry<String, Field>> entrySet = columnFields.entrySet();
        if(entrySet.size() > 0) {
            for (Entry<String, Field> entry : entrySet) {
                Field field = entry.getValue();
                Object value = ReflectUtilsDborm.getFieldValue(field, entity);
                if (value != null) {// 如果当前属性的值不是null则修改
                    columnName.append(entry.getKey());
                    columnName.append("=?, ");
                    value = convertDBType.convertToColumnValue(value);
                    bindArgs.add(value);
                }
            }
        }
        sqlContent.append(StringUtilsDborm.cutLastSign(columnName.toString(), ", "));
        sqlContent.append(" WHERE ");
        sqlContent.append(parseSql.parsePrimaryKeyWhere(entityClass));
        bindArgs.addAll(parseEntity.getPrimaryKeyFiledValues(entity));
        return PairDborm.create(sqlContent.toString(), bindArgs.toArray());
    }

    public <T> List<PairDborm<String, Object[]>> replaceDeep(T entity) {
        List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
        pairList.add(replace(entity));
        pairList.addAll(getRelationFieldPair(entity, PairType.REPLACE));
        return pairList;
    }

    public <T> List<PairDborm<String, Object[]>> saveOrUpdateDeep(T entity, Connection conn) {
        List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
        if (isExist(entity, conn)) {
            pairList.add(update(entity));
        } else {
            pairList.add(insert(entity));
        }
        pairList.addAll(getRelationFieldPair(entity, PairType.SAVEORUPDATE, conn));
        return pairList;
    }

    public <T> List<PairDborm<String, Object[]>> saveOrReplaceDeep(T entity, Connection conn) {
        List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
        if (isExist(entity, conn)) {
            pairList.add(replace(entity));
        } else {
            pairList.add(insert(entity));
        }
        pairList.addAll(getRelationFieldPair(entity, PairType.SAVEORREPLACE, conn));
        return pairList;
    }

    public PairDborm<String, String[]> getEntityCount(Class<?> entityClass) {
        // 例如： SELECT COUNT(*) FROM
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ");
        String tableName = DbormSchemaScan.getTableDomain(entityClass.getName()).getTableName();
        sql.append(tableName);
        return PairDborm.create(sql.toString(), null);
    }

    public <T> PairDborm<String, String[]> getCountByPrimaryKey(T entity) {
        // 例如： SELECT COUNT(*) FROM users WHERE user_id=?
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ");
        String[] bindArgs;
        Class<?> entityClass = entity.getClass();
        String tableName = DbormSchemaScan.getTableDomain(entityClass.getName()).getTableName();
        sql.append(tableName);
        sql.append(" WHERE ");
        sql.append(parseSql.parsePrimaryKeyWhere(entityClass));
        List<Object> primaryKeyValue = parseEntity.getPrimaryKeyFiledValues(entity);
        bindArgs = new String[primaryKeyValue.size()];
        for (int i = 0; i < primaryKeyValue.size(); i++) {
            bindArgs[i] = primaryKeyValue.get(i).toString();
        }
        return PairDborm.create(sql.toString(), bindArgs);
    }




    /**
     * 当前的级联操作类型
     */
    private enum PairType {
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
    private <T> List<PairDborm<String, Object[]>> getRelationFieldPair(T entity, PairType type) {
        List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
        Class<?> entityClass = entity.getClass();
        TableBean table = DbormSchemaScan.getTableDomain(entityClass.getName());
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
     * 获取级联对象的SQL语句对
     *
     * @param entity 对象
     * @param type   操作类型
     * @param conn   数据库连接
     * @return SQL操作集合
     * @author KEQIAO KEJI
     * @time 2013-6-5下午1:55:14
     */
    private <T> List<PairDborm<String, Object[]>> getRelationFieldPair(T entity, PairType type, Connection conn) {
        List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
        Class<?> entityClass = entity.getClass();
        TableBean table = DbormSchemaScan.getTableDomain(entityClass.getName());
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

    public Object getEntitys(Class<?> entityClass, ResultSet rs, String[] columnNames) throws SQLException {
        Map<String, Field> fields = parseEntity.getEntityAllFields(entityClass);// 获得该类的所有属性，支持联合查询
        Object entity = ReflectUtilsDborm.createInstance(entityClass);// 创建实体类的实例
        for (int i = 0; i < columnNames.length; i++) {
            Field field = fields.get(columnNames[i]);
            if (field != null) {
                Object value = convertDBType.convertToParamValue(rs, i + 1, field);
                ReflectUtilsDborm.setFieldValue(field, entity, value);
            }
        }
        return entity;
    }



    private <T> boolean isExist(T entity, Connection conn) {
        boolean result = false;
        if (entity != null) {
            ResultSet rs = null;
            try {
                PairDborm<String, String[]> pair = getCountByPrimaryKey(entity);
                rs = connectionDB.getResultSet(pair.first, pair.second, conn);
                if (rs != null && rs.next() && rs.getInt(1) > 0) {// rs.moveToNext()一定要走
                    result = true;
                }
            } catch (Exception e) {
                LogDborm.error(this.getClass().getName(), e);
            } finally {
                if(rs != null){
                    try {
                        rs.close();
                    }catch (Exception ignored){}
                }
            }
        }
        return result;
    }

}
