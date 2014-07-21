package com.keqiaokeji.dborm.core;

import com.keqiaokeji.dborm.util.DbormDataBase;
import com.keqiaokeji.dborm.util.LoggerUtils;
import com.keqiaokeji.dborm.util.PairDborm;
import com.keqiaokeji.dborm.util.StringUtilsDborm;

import java.sql.*;
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

    private static DbormDataBase dbormDataBase;


    /**
     * 新增实体（自动添加事务）
     *
     * @param entity 实体对象
     * @@return true:执行成功 false:执行失败或空的参数
     * @author KEQIAO KEJI
     * @time 2013-5-5上午10:29:57
     */
    public static <T> boolean insert(T entity) {
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
    public static <T> boolean insert(List<T> entitys) {
        boolean result = false;
        if (entitys != null && entitys.size() > 0) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
                    for (T entity : entitys) {
                        pairList.addAll(SQLPairFactory.insertDeep(entity));
                    }
                    SQLExcuter.execSQLUseTransaction(pairList, conn);
                    result = true;
                } catch (Exception e) {
                    LoggerUtils.error(e);
                } finally {
                    dbormDataBase.closeConn(conn);
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
    public static <T> boolean update(T entity) {
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
    public static <T> boolean update(List<T> entitys) {
        boolean result = false;
        if (entitys != null && entitys.size() > 0) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
                    for (T entity : entitys) {
                        pairList.addAll(SQLPairFactory.updateDeep(entity));
                    }
                    SQLExcuter.execSQLUseTransaction(pairList, conn);
                    result = true;
                } catch (Exception e) {
                    LoggerUtils.error(e);
                } finally {
                    dbormDataBase.closeConn(conn);
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
    public static <T> boolean delete(T entity) {
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
    public static <T> boolean delete(List<T> entitys) {
        boolean result = false;
        if (entitys != null && entitys.size() > 0) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
                    for (T entity : entitys) {
                        pairList.addAll(SQLPairFactory.deleteDeep(entity));
                    }
                    SQLExcuter.execSQLUseTransaction(pairList, conn);
                    result = true;
                } catch (Exception e) {
                    LoggerUtils.error(e);
                } finally {
                    dbormDataBase.closeConn(conn);
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
    public static <T> boolean replace(T entity) {
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
    public static <T> boolean replace(List<T> entitys) {
        boolean result = false;
        if (entitys != null && entitys.size() > 0) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
                    for (T entity : entitys) {
                        pairList.addAll(SQLPairFactory.replaceDeep(entity));
                    }
                    SQLExcuter.execSQLUseTransaction(pairList, conn);
                    result = true;
                } catch (Exception e) {
                    LoggerUtils.error(e);
                } finally {
                    dbormDataBase.closeConn(conn);
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
    public static <T> boolean saveOrReplace(T entity) {
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
    public static <T> boolean saveOrReplace(List<T> entitys) {
        boolean result = false;
        if (entitys != null && entitys.size() > 0) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
                    for (T entity : entitys) {
                        pairList.addAll(SQLPairFactory.saveOrReplaceDeep(entity, conn));
                    }
                    SQLExcuter.execSQLUseTransaction(pairList, conn);
                    result = true;
                } catch (Exception e) {
                    LoggerUtils.error(e);
                } finally {
                    dbormDataBase.closeConn(conn);
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
    public static <T> boolean saveOrUpdate(T entity) {
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
    public static <T> boolean saveOrUpdate(List<T> entitys) {
        boolean result = false;
        if (entitys != null && entitys.size() > 0) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    List<PairDborm<String, Object[]>> pairList = new ArrayList<PairDborm<String, Object[]>>();
                    for (T entity : entitys) {
                        pairList.addAll(SQLPairFactory.saveOrUpdateDeep(entity, conn));
                    }
                    SQLExcuter.execSQLUseTransaction(pairList, conn);
                    result = true;
                } catch (Exception e) {
                    LoggerUtils.error(e);
                } finally {
                    dbormDataBase.closeConn(conn);
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
    public static <T> T getEntitie(String sql, String[] bindArgs, Class<?> entityClass) {
        T result = null;
        Connection conn = getConnection();
        if (conn != null) {
            try {
                result = getEntitie(sql, bindArgs, entityClass, conn);
            } catch (Exception e) {
                LoggerUtils.error(e);
            } finally {
                dbormDataBase.closeConn(conn);
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
    public static <T> T getEntitie(String sql, String[] bindArgs, Class<?> entityClass, Connection conn) {
        if (StringUtilsDborm.isNotBlank(sql) && entityClass != null && conn != null) {
            try {
                List<T> entityList = getEntities(sql, bindArgs, entityClass, conn);
                if (entityList != null && entityList.size() > 0) {
                    return entityList.get(0);
                }
            } catch (Exception e) {
                LoggerUtils.error(e);
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
    public static <T> List<T> getEntities(String sql, String[] bindArgs, Class<?> entityClass) {
        List<T> results = new ArrayList<T>();
        Connection conn = getConnection();
        if (conn != null) {
            try {
                results = getEntities(sql, bindArgs, entityClass, conn);
            } catch (Exception e) {
                LoggerUtils.error(e);
            } finally {
                dbormDataBase.closeConn(conn);
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
    public static <T> List<T> getEntities(String sql, Object[] bindArgs, Class<?> entityClass, Connection conn) {
        List<T> results = new ArrayList<T>();
        if (StringUtilsDborm.isNotBlank(sql) && entityClass != null && conn != null) {
            ResultSet rs = null;
            try {
                rs = SQLExcuter.getResultSet(sql, bindArgs, conn);
                if (rs != null) {
                    String[] columnNames = getColumnNames(rs);
                    while (rs.next()) {
                        Object entity = EntityResolver.getEntitys(entityClass, rs, columnNames);
                        results.add((T) entity);
                    }
                }
            } catch (Exception e) {
                LoggerUtils.error(e);
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
    public static List<Map<String, Object>> getEntities(String sql, String[] bindArgs, Class<?>[] entityClasses) {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        if (StringUtilsDborm.isNotBlank(sql) && entityClasses != null && entityClasses.length > 0) {
            Connection conn = getConnection();
            if (conn != null) {
                ResultSet rs = null;
                try {
                    rs = SQLExcuter.getResultSet(sql, bindArgs, conn);
                    if (rs != null) {
                        String[] columnNames = getColumnNames(rs);
                        while (rs.next()) {// 遍历每一行记录
                            Map<String, Object> entityTeam = new HashMap<String, Object>();// 实体组
                            for (Class<?> entityClass : entityClasses) {// 对每一个对象实例化
                                Object entity = EntityResolver.getEntitys(entityClass, rs, columnNames);
                                entityTeam.put(entityClass.getName(), entity);
                            }
                            results.add(entityTeam);
                        }
                    }
                } catch (Exception e) {
                    LoggerUtils.error(e);
                } finally {
                    closeRs(rs);
                }
            }
        }
        return results;
    }

    /**
     * 根据实例模版查询（根据实例对象中属性值不为空的属性做过滤条件）
     *
     * @param example 实例模版
     * @param <T>     实例类型
     * @return 实体集合或无实体的list集合
     */
    public static <T> List<T> getEntitiesByExample(T example) {
        List<T> results = new ArrayList<T>();
        Connection conn = getConnection();
        if (conn != null) {
            try {
                PairDborm<String, Object[]> pair = SQLPairFactory.getEntitiesByExample(example);
                results = getEntities(pair.first, pair.second, example.getClass(), conn);
            } catch (Exception e) {
                LoggerUtils.error(e);
            } finally {
                dbormDataBase.closeConn(conn);
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
    public static <T> List<T> getEntities(String sql, String[] bindArgs, ResultMapper<T> mapper) {
        List<T> results = new ArrayList<T>();
        DbormDataBase dbormDataBase = Dborm.getDbormDataBase();
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = dbormDataBase.getConnection();
            rs = SQLExcuter.getResultSet(sql, bindArgs, conn);
            while (rs.next()) {
                results.add(mapper.map(rs));
            }
        } catch (Exception e) {
            LoggerUtils.error(Dborm.class.getName(), e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                dbormDataBase.closeConn(conn);
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
    public static <T> boolean isExist(T entity) {
        boolean result = false;
        if (entity != null) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    result = isExist(entity, conn);
                } catch (Exception e) {
                    LoggerUtils.error(e);
                } finally {
                    dbormDataBase.closeConn(conn);
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
    public static <T> boolean isExist(T entity, Connection conn) {
        boolean result = false;
        if (entity != null) {
            ResultSet rs = null;
            try {
                PairDborm<String, Object[]> pair = SQLPairFactory.getCountByPrimaryKey(entity);
                rs = SQLExcuter.getResultSet(pair.first, pair.second, conn);
                if (rs != null && rs.next() && rs.getInt(1) > 0) {// rs.moveToNext()一定要走
                    result = true;
                }
            } catch (Exception e) {
                LoggerUtils.error(e);
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
    public static int getEntityCount(Class<?> entityClass) {
        int count = 0;
        if (entityClass != null) {
            Connection conn = getConnection();
            if (conn != null) {
                ResultSet rs = null;
                try {
                    PairDborm<String, Object[]> pair = SQLPairFactory.getEntityCount(entityClass);
                    rs = SQLExcuter.getResultSet(pair.first, pair.second, conn);
                    if (rs != null) {
                        rs.next();
                        count = rs.getInt(1);
                    }
                } catch (Exception e) {
                    LoggerUtils.error(e);
                } finally {
                    closeRs(rs);
                    dbormDataBase.closeConn(conn);
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
    public static int getCount(String sql, String[] selectionArgs) {
        int count = 0;
        if (StringUtilsDborm.isNotBlank(sql)) {
            Connection conn = getConnection();
            if (conn != null) {
                ResultSet rs = null;
                try {
                    rs = SQLExcuter.getResultSet(sql, selectionArgs, conn);
                    rs.next();
                    count = rs.getInt(1);
                } catch (Exception e) {
                    LoggerUtils.error(e);
                } finally {
                    closeRs(rs);
                    dbormDataBase.closeConn(conn);
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
    public static boolean execSql(String sql) {
        boolean result = false;
        if (StringUtilsDborm.isNotBlank(sql)) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    SQLExcuter.execSQL(sql, null, conn);
                    result = true;
                } catch (Exception e) {
                    LoggerUtils.error(e);
                } finally {
                    dbormDataBase.closeConn(conn);
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
    public static boolean execSql(String sql, Object[] bindArgs) {
        boolean result = false;
        if (StringUtilsDborm.isNotBlank(sql)) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    execSql(sql, bindArgs, conn);
                    result = true;
                } catch (Exception e) {
                    LoggerUtils.error(e);
                } finally {
                    dbormDataBase.closeConn(conn);
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
    public static boolean execSql(String sql, Object[] bindArgs, Connection conn) {
        boolean result = false;
        if (StringUtilsDborm.isNotBlank(sql)) {
            try {
                SQLExcuter.execSQL(sql, bindArgs, conn);
                result = true;
            } catch (Exception e) {
                LoggerUtils.error(e);
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
    public static boolean execSql(List<PairDborm<String, Object[]>> execSqlPairList) {
        boolean result = false;
        if (execSqlPairList != null && execSqlPairList.size() > 0) {
            Connection conn = getConnection();
            if (conn != null) {
                try {
                    SQLExcuter.execSQLUseTransaction(execSqlPairList, conn);
                    result = true;
                } catch (Exception e) {
                    LoggerUtils.error(e);
                } finally {
                    dbormDataBase.closeConn(conn);
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
    public static Connection getConnection() {
        if (dbormDataBase != null) {
            return dbormDataBase.getConnection();
        } else {
            return null;
        }
    }


    private static String[] getColumnNames(ResultSet rs) throws SQLException {
        ResultSetMetaData resultSetMetaData = rs.getMetaData();
        int count = resultSetMetaData.getColumnCount();
        String[] columnNames = new String[count];
        for (int i = 0; i < count; i++) {
            columnNames[i] = resultSetMetaData.getColumnName(i + 1);
        }
        return columnNames;
    }


    private static void closeRs(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static <T> List<T> entityToEntityList(T entity) {
        List<T> entitys = new ArrayList<T>();
        entitys.add(entity);
        return entitys;
    }

    public static DbormDataBase getDbormDataBase() {
        return dbormDataBase;
    }

    public static void setDbormDataBase(DbormDataBase dbormDataBase) {
        Dborm.dbormDataBase = dbormDataBase;
    }
}
