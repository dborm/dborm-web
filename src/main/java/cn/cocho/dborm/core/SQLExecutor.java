package cn.cocho.dborm.core;

import cn.cocho.dborm.util.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 连接数据库及执行SQL
 *
 * @author KEQIAO KEJI
 * @time 2013-5-6上午10:40:40
 */
public class SQLExecutor {

    /**
     * 执行SQL(并作SQL检查及输出)
     *
     * @param sql      sql语句
     * @param bindArgs 该SQL语句所需的参数
     * @param conn     数据库连接
     * @throws SQLException
     * @author KEQIAO KEJI
     * @time 2013-6-7下午2:54:48
     */
    public void execSQL(String sql, Object[] bindArgs, Connection conn) throws SQLException {
        checkSql(sql, bindArgs);
        PreparedStatement pst = null;
        try {
            pst = conn.prepareStatement(sql);
            if (bindArgs != null) {
                for (int i = 0; i < bindArgs.length; i++) {
                    pst.setObject(i + 1, bindArgs[i]);
                }
            }
            pst.executeUpdate();
        } finally {
            if (pst != null) {
                pst.close();
            }
        }
    }

    /**
     * 批量执行SQL，在事务中完成
     *
     * @param execSqlPairList 第一个参数为SQL语句， 第二个参数为SQL语句所需的参数
     * @param conn            数据库连接
     * @throws SQLException
     * @author KEQIAO KEJI
     * @time 2013-5-6上午10:41:26
     */
    public void execSQLUseTransaction(List<PairDborm<String, Object[]>> execSqlPairList, Connection conn) throws SQLException {
        PreparedStatement pst = null;
        try {
            conn.setAutoCommit(false);
            for (PairDborm<String, Object[]> pair : execSqlPairList) {
                checkSql(pair.first, pair.second);
                pst = conn.prepareStatement(pair.first);
                if (pair.second != null) {
                    for (int x = 0; x < pair.second.length; x++) {
                        pst.setObject(x + 1, pair.second[x]);
                    }
                }
                pst.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            if (pst != null) {
                pst.close();
            }
        }
    }

    /**
     * 查询操作
     *
     * @param sql      查询语句
     * @param bindArgs 查询语句所需的参数
     * @param conn     数据库连接
     * @return 查询结果集或null
     * @throws SQLException
     * @author KEQIAO KEJI
     * @time 2013-5-6上午10:43:44
     */
    public ResultSet getResultSet(String sql, Object[] bindArgs, Connection conn) throws SQLException {
        ResultSet result;
        checkSql(sql, bindArgs);
        PreparedStatement pst = conn.prepareStatement(sql);
        if (bindArgs != null) {
            for (int i = 0; i < bindArgs.length; i++) {
                pst.setObject(i + 1, bindArgs[i]);
            }
        }
        result = pst.executeQuery();
        return result;
    }


    /**
     * 检查SQL语句并做日志记录
     *
     * @param sql      sql语句
     * @param bindArgs sql语句所绑定的参数
     * @author KEQIAO KEJI
     * @time 2013-5-7上午10:55:38
     */
    private void checkSql(String sql, Object[] bindArgs) {
        if (new StringUtilsDborm().isNotBlank(sql)) {
            if (DbormContexts.showSql) {
                StringBuilder sqlContent = new StringBuilder("运行的SQL语句如下：\n");
                sqlContent.append(sql);
                if (bindArgs != null) {
                    sqlContent.append("\n所需的参数如下：\n");
                    int size = bindArgs.length;
                    for (int i = 0; i < size; i++) {
                        sqlContent.append(bindArgs[i]);
                        if (i + 1 != size) {
                            sqlContent.append(",");
                        }
                    }
                }
                new LoggerUtilsDborm().debug(sqlContent.toString());
            }
        } else {
            throw new IllegalArgumentException("需要执行的SQL语句不能为空!");
        }
    }


}
