package cn.cocho.dborm.core;

import cn.cocho.dborm.util.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通过Dborm深度操作数据库（级联操作数据库，自动添加事务，操作成功返回true,操作失败返回false）
 *
 * @author KEQIAO KEJI
 * @time 2013-5-6上午11:25:34
 */
public class Dborm {

    SQLPairFactory sqlPairFactory = new SQLPairFactory(this);
    SQLExecutor sqlExecutor = new SQLExecutor();
    LoggerUtilsDborm loggerUtils = new LoggerUtilsDborm();
    StringUtilsDborm stringUtils = new StringUtilsDborm();
    EntityResolver entityResolver = new EntityResolver();

    private DbormDataBase dataBase;

    public Dborm(DbormDataBase dataBase) {
        this.dataBase = dataBase;
    }

    public Dborm(DbormDataBase dataBase, DbormLogger logger){
        this.dataBase = dataBase;
        DbormContexts.log = logger;
        DbormContexts.showSql = true;
    }


    /**
     * 新增实体（自动添加事务）
     *
     * @param entity 实体对象
     * @@return true:执行成功 false:执行失败或空的参数
     * @author KEQIAO KEJI
     * @time 2013-5-5上午10:29:57
     */
    public <T> boolean insert(T entity) {
        return insert(entityToEntityList(entity));
    }


    /**
     * 批量新增实体（自动添加事务）
     *
     * @param entitys 实体对象集合
     * @@return true:执行成功 false:执行失败或空的参数
     * @author KEQIAO KEJI
     * @time 2013-5-5上午10:30:15
     */
    public <T> boolean insert(List<T> entitys) {
        boolean result = false;
        if (entitys != null && entitys.size() > 0) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
                    for (T entity : entitys) {
                        pairList.addAll(sqlPairFactory.insertDeep(entity));
                    }
                    sqlExecutor.execSQLUseTransaction(pairList, conn);
                    result = true;
                } catch (Exception e) {
                    loggerUtils.error(e);
                } finally {
                    dataBase.closeConn(conn);
                }
            }
        }
        return result;
    }

    /**
     * 修改实体(主键值不能为空，自动添加事务)
     *
     * @param entity 实体对象
     * @@return true:执行成功 false:执行失败或空的参数
     * @author KEQIAO KEJI
     * @time 2013-5-5上午10:39:58
     */
    public <T> boolean update(T entity) {
        return update(entityToEntityList(entity));
    }

    /**
     * 批量修改实体(主键值不能为空，自动添加事务)
     *
     * @param entitys 实体对象集合
     * @@return true:执行成功 false:执行失败或空的参数
     * @author KEQIAO KEJI
     * @time 2013-5-5上午10:40:14
     */
    public <T> boolean update(List<T> entitys) {
        boolean result = false;
        if (entitys != null && entitys.size() > 0) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
                    for (T entity : entitys) {
                        pairList.addAll(sqlPairFactory.updateDeep(entity));
                    }
                    sqlExecutor.execSQLUseTransaction(pairList, conn);
                    result = true;
                } catch (Exception e) {
                    loggerUtils.error(e);
                } finally {
                    dataBase.closeConn(conn);
                }
            }
        }
        return result;
    }

    /**
     * 删除实体(主键值不能为空，自动添加事务)
     *
     * @param entity 实体对象
     * @@return true:执行成功 false:执行失败或空的参数
     * @author KEQIAO KEJI
     * @time 2013-5-5上午10:40:31
     */
    public <T> boolean delete(T entity) {
        return delete(entityToEntityList(entity));
    }

    /**
     * 批量删除实体(主键值不能为空，自动添加事务)
     *
     * @param entitys 实体对象集合
     * @@return true:执行成功 false:执行失败或空的参数
     * @author KEQIAO KEJI
     * @time 2013-5-5上午10:41:09
     */
    public <T> boolean delete(List<T> entitys) {
        boolean result = false;
        if (entitys != null && entitys.size() > 0) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
                    for (T entity : entitys) {
                        pairList.addAll(sqlPairFactory.deleteDeep(entity));
                    }
                    sqlExecutor.execSQLUseTransaction(pairList, conn);
                    result = true;
                } catch (Exception e) {
                    loggerUtils.error(e);
                } finally {
                    dataBase.closeConn(conn);
                }
            }
        }
        return result;
    }

    /**
     * 替换实体（修改属性值不为null的属性，主键值不能为空，自动添加事务）
     *
     * @param entity 实体对象
     * @@return true:执行成功 false:执行失败或空的参数
     * @author KEQIAO KEJI
     * @time 2013-5-5上午10:44:02
     */
    public <T> boolean replace(T entity) {
        return replace(entityToEntityList(entity));
    }

    /**
     * 批量替换实体（修改属性值不为null的属性，主键值不能为空，自动添加事务）
     *
     * @param entitys 实体对象集合
     * @@return true:执行成功 false:执行失败或空的参数
     * @author KEQIAO KEJI
     * @time 2013-5-5上午10:47:17
     */
    public <T> boolean replace(List<T> entitys) {
        boolean result = false;
        if (entitys != null && entitys.size() > 0) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
                    for (T entity : entitys) {
                        pairList.addAll(sqlPairFactory.replaceDeep(entity));
                    }
                    sqlExecutor.execSQLUseTransaction(pairList, conn);
                    result = true;
                } catch (Exception e) {
                    loggerUtils.error(e);
                } finally {
                    dataBase.closeConn(conn);
                }
            }
        }
        return result;
    }

    /**
     * 新增或替换（根据主键查找数据库是否有该记录，有则替换，没有则新增，自动添加事务）
     *
     * @param entity 实体类
     * @@return true:执行成功 false:执行失败或空的参数或空的实体
     * @author KEQIAO KEJI
     * @time 2013-5-6下午3:22:18
     */
    public <T> boolean saveOrReplace(T entity) {
        return saveOrReplace(entityToEntityList(entity));
    }

    /**
     * 批量新增或替换（根据主键查找数据库是否有该记录，有则替换，没有则新增，自动添加事务）
     *
     * @param entitys 实体类集合
     * @@return true:执行成功 false:执行失败或空的参数
     * @author KEQIAO KEJI
     * @time 2013-5-6下午3:22:59
     */
    public <T> boolean saveOrReplace(List<T> entitys) {
        boolean result = false;
        if (entitys != null && entitys.size() > 0) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
                    for (T entity : entitys) {
                        pairList.addAll(sqlPairFactory.saveOrReplaceDeep(entity, conn));
                    }
                    sqlExecutor.execSQLUseTransaction(pairList, conn);
                    result = true;
                } catch (Exception e) {
                    loggerUtils.error(e);
                } finally {
                    dataBase.closeConn(conn);
                }
            }
        }
        return result;
    }

    /**
     * 新增或修改（根据主键查找数据库是否有该记录，有则修改，没有则新增，自动添加事务）
     *
     * @param entity 实体类
     * @@return true:执行成功 false:执行失败或空的参数
     * @author KEQIAO KEJI
     * @time 2013-5-6下午3:23:22
     */
    public <T> boolean saveOrUpdate(T entity) {
        return saveOrUpdate(entityToEntityList(entity));
    }

    /**
     * 批量新增或修改（根据主键查找数据库是否有该记录，有则修改，没有则新增，自动添加事务）
     *
     * @param entitys 实体类集合
     * @@return true:执行成功 false:执行失败或空的参数
     * @author KEQIAO KEJI
     * @time 2013-5-6下午3:23:22
     */
    public <T> boolean saveOrUpdate(List<T> entitys) {
        boolean result = false;
        if (entitys != null && entitys.size() > 0) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
                    for (T entity : entitys) {
                        pairList.addAll(sqlPairFactory.saveOrUpdateDeep(entity, conn));
                    }
                    sqlExecutor.execSQLUseTransaction(pairList, conn);
                    result = true;
                } catch (Exception e) {
                    loggerUtils.error(e);
                } finally {
                    dataBase.closeConn(conn);
                }
            }
        }
        return result;
    }


    /**
     * 根据查询语句返回实体(如果查询出多个实体时仅返回第一个)
     *
     * @param sql         查询语句
     * @param bindArgs    查询语句所需的参数（该参数允许为null）
     * @param entityClass 返回的实体类型
     * @return 实体或null
     * @author KEQIAO KEJI
     * @time 2013-5-6上午11:23:46
     */
    public <T> T getEntity(String sql, Object[] bindArgs, Class<?> entityClass) {
        T result = null;
        Connection conn = getConnection();
        if (conn != null) {
            try {
                result = getEntity(sql, bindArgs, entityClass, conn);
            } catch (Exception e) {
                loggerUtils.error(e);
            } finally {
                dataBase.closeConn(conn);
            }
        }
        return result;
    }

    /**
     * 根据查询语句返回实体(如果查询出多个实体时仅返回第一个)
     *
     * @param sql         查询语句
     * @param bindArgs    查询语句所需的参数（该参数允许为null）
     * @param entityClass 返回的实体类型
     * @param conn        数据库连接
     * @return 实体或null
     * @author KEQIAO KEJI
     * @time 2013-5-6上午11:23:46
     */
    public <T> T getEntity(String sql, Object[] bindArgs, Class<?> entityClass, Connection conn) {
        if (stringUtils.isNotBlank(sql) && entityClass != null && conn != null) {
            try {
                List<T> entityList = getEntities(sql, bindArgs, entityClass, conn);
                if (entityList != null && entityList.size() > 0) {
                    return entityList.get(0);
                }
            } catch (Exception e) {
                loggerUtils.error(e);
            }
        }
        return null;
    }

    /**
     * 根据查询语句返回实体集合
     *
     * @param sql         查询语句
     * @param bindArgs    查询语句所需的参数（该参数允许为null）
     * @param entityClass 返回的实体类型
     * @return 实体集合或无实体的list集合
     * @author KEQIAO KEJI
     * @time 2013-5-6上午11:23:46
     */
    public <T> List<T> getEntities(String sql, Object[] bindArgs, Class<?> entityClass) {
        List<T> results = new ArrayList<T>();
        Connection conn = getConnection();
        if (conn != null) {
            try {
                results = getEntities(sql, bindArgs, entityClass, conn);
            } catch (Exception e) {
                loggerUtils.error(e);
            } finally {
                dataBase.closeConn(conn);
            }
        }
        return results;
    }

    /**
     * 根据查询语句返回实体集合
     *
     * @param sql         查询语句
     * @param bindArgs    查询语句所需的参数（该参数允许为null）
     * @param entityClass 返回的实体类型
     * @param conn        数据库连接
     * @return 实体集合或无实体的list集合
     * @author KEQIAO KEJI
     * @time 2013-5-6上午11:23:46
     */
    public <T> List<T> getEntities(String sql, Object[] bindArgs, Class<?> entityClass, Connection conn) {
        List<T> results = new ArrayList<T>();
        if (stringUtils.isNotBlank(sql) && entityClass != null && conn != null) {
            ResultSet rs = null;
            try {
                rs = sqlExecutor.getResultSet(sql, bindArgs, conn);
                if (rs != null) {
                    String[] columnNames = getColumnNames(rs);
                    while (rs.next()) {
                        Object entity = entityResolver.getEntitys(entityClass, rs, columnNames);
                        results.add((T) entity);
                    }
                }
            } catch (Exception e) {
                loggerUtils.error(e);
            } finally {
                closeRs(rs);
            }
        }
        return results;
    }

    /**
     * 多表联合查询
     *
     * @param sql           查询语句
     * @param bindArgs      查询语句所需的参数（该参数允许为null）
     * @param entityClasses 实体类集合
     * @return 实体集合或无实体的list集合
     * @author KEQIAO KEJI
     * @time 2013-6-7上午10:42:18
     */
    public List<Map<String, Object>> getEntities(String sql, Object[] bindArgs, Class<?>[] entityClasses) {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        if (stringUtils.isNotBlank(sql) && entityClasses != null && entityClasses.length > 0) {
            Connection conn = getConnection();
            if (conn != null) {
                ResultSet rs = null;
                try {
                    rs = sqlExecutor.getResultSet(sql, bindArgs, conn);
                    if (rs != null) {
                        String[] columnNames = getColumnNames(rs);
                        while (rs.next()) {// 遍历每一行记录
                            Map<String, Object> entityTeam = new HashMap<String, Object>();// 实体组
                            for (Class<?> entityClass : entityClasses) {// 对每一个对象实例化
                                Object entity = entityResolver.getEntitys(entityClass, rs, columnNames);
                                entityTeam.put(entityClass.getName(), entity);
                            }
                            results.add(entityTeam);
                        }
                    }
                } catch (Exception e) {
                    loggerUtils.error(e);
                } finally {
                    closeRs(rs);
                }
            }
        }
        return results;
    }

    /**
     * 根据实例模版查询（根据实例对象中属性值不为空的属性做过滤条件，默认情况下，添加之间是AND关系）
     *
     * @param example 实例模版
     * @param <T>     实例类型
     * @return 实体对象(如果有多个实体对象则返回第一个)或null
     */
    public <T> T getEntityByExample(T example) {
        return getEntityByExample(example, true);
    }

    /**
     * 根据实例模版查询（根据实例对象中属性值不为空的属性做过滤条件）
     *
     * @param example 实例模版
     * @param isAnd   true：使用AND连接多个条件，false：使用OR连接多个条件
     * @param <T>     实例类型
     * @return 实体对象(如果有多个实体对象则返回第一个)或null
     */
    public <T> T getEntityByExample(T example, boolean isAnd) {
        if (example != null) {
            try {
                List<T> entityList = getEntitiesByExample(example, isAnd);
                if (entityList != null && entityList.size() > 0) {
                    return entityList.get(0);
                }
            } catch (Exception e) {
                loggerUtils.error(e);
            }
        }
        return null;
    }

    /**
     * 根据实例模版查询（根据实例对象中属性值不为空的属性做过滤条件，默认情况下，添加之间是AND关系）
     *
     * @param example 实例模版
     * @param <T>     实例类型
     * @return 实体集合或无实体的list集合
     */
    public <T> List<T> getEntitiesByExample(T example) {
        return getEntitiesByExample(example, true);
    }

    /**
     * 根据实例模版查询（根据实例对象中属性值不为空的属性做过滤条件）
     *
     * @param example 实例模版
     * @param isAnd   true：使用AND连接多个条件，false：使用OR连接多个条件
     * @param <T>     实例类型
     * @return 实体集合或无实体的list集合
     */
    public <T> List<T> getEntitiesByExample(T example, boolean isAnd) {
        List<T> results = new ArrayList<T>();
        Connection conn = getConnection();
        if (conn != null) {
            try {
                PairDborm<String, Object[]> pair = sqlPairFactory.getEntitiesByExample(example, isAnd);
                results = getEntities(pair.first, pair.second, example.getClass(), conn);
            } catch (Exception e) {
                loggerUtils.error(e);
            } finally {
                dataBase.closeConn(conn);
            }
        }
        return results;
    }

    /**
     * 自定义数据映射规则
     *
     * @param <T> 数据的类型
     */
    public interface ResultMapper<T> {
        T map(ResultSet rs);
    }

    /**
     * 根据查询语句及自定义结果集转换类返回实体集合
     *
     * @param sql      查询语句
     * @param bindArgs 查询语句所需的参数（该参数允许为null）
     * @param mapper   自定义的结果集与对象转换的类
     * @param <T>      结果集类型
     * @return 实体集合或无实体的list集合
     */
    public <T> List<T> getEntities(String sql, Object[] bindArgs, ResultMapper<T> mapper) {
        List<T> results = new ArrayList<T>();
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = dataBase.getConnection();
            rs = sqlExecutor.getResultSet(sql, bindArgs, conn);
            while (rs.next()) {
                results.add(mapper.map(rs));
            }
        } catch (Exception e) {
            loggerUtils.error(Dborm.class.getName(), e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                dataBase.closeConn(conn);
            } catch (Exception ignored) {
            }
        }
        return results;
    }


    /**
     * 根据对象主键判断对象是否存在
     *
     * @param entity 实体对象
     * @return true：存在;false：不存在
     * @author KEQIAO KEJI
     * @time 2013-5-15上午11:29:16
     */
    public <T> boolean isExist(T entity) {
        boolean result = false;
        if (entity != null) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    result = isExist(entity, conn);
                } catch (Exception e) {
                    loggerUtils.error(e);
                } finally {
                    dataBase.closeConn(conn);
                }
            }
        }
        return result;
    }

    /**
     * 根据对象主键判断对象是否存在
     *
     * @param entity 实体对象
     * @param conn   数据库操作对象
     * @return true：存在;false：不存在
     * @author KEQIAO KEJI
     */
    public <T> boolean isExist(T entity, Connection conn) {
        boolean result = false;
        if (entity != null) {
            ResultSet rs = null;
            try {
                PairDborm<String, Object[]> pair = sqlPairFactory.getCountByPrimaryKey(entity);
                rs = sqlExecutor.getResultSet(pair.first, pair.second, conn);
                if (rs != null && rs.next() && rs.getInt(1) > 0) {// rs.moveToNext()一定要走
                    result = true;
                }
            } catch (Exception e) {
                loggerUtils.error(e);
            } finally {
                closeRs(rs);
            }
        }
        return result;
    }

    /**
     * 获得实体类的记录条数
     *
     * @param entityClass 实体类
     * @return 条数
     * @author KEQIAO KEJI
     * @time 2013-6-6下午5:23:13
     */
    public int getEntityCount(Class<?> entityClass) {
        int count = 0;
        if (entityClass != null) {
            Connection conn = getConnection();
            if (conn != null) {
                ResultSet rs = null;
                try {
                    PairDborm<String, Object[]> pair = sqlPairFactory.getEntityCount(entityClass);
                    rs = sqlExecutor.getResultSet(pair.first, pair.second, conn);
                    if (rs != null) {
                        rs.next();
                        count = rs.getInt(1);
                    }
                } catch (Exception e) {
                    loggerUtils.error(e);
                } finally {
                    closeRs(rs);
                    dataBase.closeConn(conn);
                }
            }
        }
        return count;
    }

    /**
     * 查询行数
     *
     * @param sql           查询行数的SQL语句（必须是select count(*) from ...）
     * @param selectionArgs SQL语句所需参数（该参数允许为null）
     * @return 行数
     * @author KEQIAO KEJI
     * @time 2013-5-15上午11:32:30
     */
    public int getCount(String sql, Object[] selectionArgs) {
        int count = 0;
        if (stringUtils.isNotBlank(sql)) {
            Connection conn = getConnection();
            if (conn != null) {
                ResultSet rs = null;
                try {
                    rs = sqlExecutor.getResultSet(sql, selectionArgs, conn);
                    rs.next();
                    count = rs.getInt(1);
                } catch (Exception e) {
                    loggerUtils.error(e);
                } finally {
                    closeRs(rs);
                    dataBase.closeConn(conn);
                }
            }
        }
        return count;
    }

    /**
     * 执行SQL
     *
     * @param sql sql语句
     * @@return true:执行成功 false:执行失败或空的参数
     * @author KEQIAO KEJI
     */
    public boolean execSql(String sql) {
        boolean result = false;
        if (stringUtils.isNotBlank(sql)) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    sqlExecutor.execSQL(sql, null, conn);
                    result = true;
                } catch (Exception e) {
                    loggerUtils.error(e);
                } finally {
                    dataBase.closeConn(conn);
                }
            }
        }
        return result;
    }


    /**
     * 执行SQL
     *
     * @param sql      SQL语句
     * @param bindArgs SQL语句所需的参数（该参数允许为null）
     * @@return true:执行成功 false:执行失败或空的参数
     * @author KEQIAO KEJI
     * @time 2013-5-6下午4:23:11
     */
    public boolean execSql(String sql, Object[] bindArgs) {
        boolean result = false;
        if (stringUtils.isNotBlank(sql)) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    execSql(sql, bindArgs, conn);
                    result = true;
                } catch (Exception e) {
                    loggerUtils.error(e);
                } finally {
                    dataBase.closeConn(conn);
                }
            }
        }
        return result;
    }

    /**
     * 执行指定的SQL语句
     *
     * @param sql      sql语句
     * @param bindArgs sql语句所需的参数
     * @param conn     数据库连接
     * @return true:执行成功 false:执行失败或空的参数
     */
    public boolean execSql(String sql, Object[] bindArgs, Connection conn) {
        boolean result = false;
        if (stringUtils.isNotBlank(sql)) {
            try {
                sqlExecutor.execSQL(sql, bindArgs, conn);
                result = true;
            } catch (Exception e) {
                loggerUtils.error(e);
                result = false;
            }
        }
        return result;
    }

    /**
     * 按事务方式批量执行SQL
     *
     * @param execSqlPairList sql语句集合
     * @@return true:执行成功 false:执行失败或空的参数
     * @author KEQIAO KEJI
     * @time 2013-6-7下午3:08:45
     */
    public boolean execSql(List<PairDborm<String, Object[]>> execSqlPairList) {
        boolean result = false;
        if (execSqlPairList != null && execSqlPairList.size() > 0) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    sqlExecutor.execSQLUseTransaction(execSqlPairList, conn);
                    result = true;
                } catch (Exception e) {
                    loggerUtils.error(e);
                } finally {
                    dataBase.closeConn(conn);
                }
            }
        }
        return result;
    }

    /**
     * 获得数据库连接
     *
     * @return 数据库连接或null
     */
    public Connection getConnection() {
        if (dataBase != null) {
            return dataBase.getConnection();
        } else {
            return null;
        }
    }


    private String[] getColumnNames(ResultSet rs) throws SQLException {
        ResultSetMetaData resultSetMetaData = rs.getMetaData();
        int count = resultSetMetaData.getColumnCount();
        String[] columnNames = new String[count];
        for (int i = 0; i < count; i++) {
            columnNames[i] = resultSetMetaData.getColumnName(i + 1);
        }
        return columnNames;
    }


    private void closeRs(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        }
    }

    private <T> List<T> entityToEntityList(T entity) {
        List<T> entitys = new ArrayList<T>();
        entitys.add(entity);
        return entitys;
    }

    public DbormDataBase getDataBase() {
        return dataBase;
    }

    public void setDataBase(DbormDataBase dataBase) {
        this.dataBase = dataBase;
    }
}
