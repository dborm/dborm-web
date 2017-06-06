package com.tbc.paas.mdl.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.tbc.paas.mdl.util.MdlUtil;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.tbc.paas.mdl.cfg.Configure;
import com.tbc.paas.mdl.domain.MdlException;
import com.tbc.paas.mdl.ds.MdlConnection;
import com.tbc.paas.mdl.ds.MdlDataSource;
//import com.tbc.paas.mdl.ds.MdlDataSource;
import com.tbc.paas.mdl.impl.rowmapper.MdlEntityEncopRowMapper;
import com.tbc.paas.mdl.impl.rowmapper.MdlEntityRelRowMapper;
import com.tbc.paas.mdl.impl.rowmapper.MdlMapEntityRowMapper;
import com.tbc.paas.mdl.impl.rowmapper.MdlMutilEntityEncopRowMapper;
import com.tbc.paas.mdl.impl.rowmapper.MdlUnencopRowMapper;
import com.tbc.paas.mdl.log.MdlLogInfo;
import com.tbc.paas.mdl.log.MdlLogger;
import com.tbc.paas.mql.domain.SqlResultColumn;
import com.tbc.paas.mql.util.SqlBuilder;

/**
 * 这个类封装MDL对最基本的数据与操作，主要是查询和更新的基本封装。
 *
 * @author 田振
 */
public class MdlJdbcTemplate extends JdbcDaoSupport {

    public static final Logger LOG = Logger.getLogger(MdlJdbcTemplate.class);

    // 定义配置对象
    protected Configure configure;
    private boolean useShareCenter = true;

    public MdlJdbcTemplate() {
        super();
    }

    /**
     * 该方法依据实体间的关系查询出按实体关系封装后的结果。
     *
     * @param sqlBuilder       查询语句
     * @param sqlResultColumns 查询记过的列元数据信息。
     * @param mainKey          查询的主实体的别名。
     * @param shared           是否需要使用共享中心查询。
     * @return 查询结果。
     */

    public <T> List<T> query(SqlBuilder sqlBuilder,
                             List<SqlResultColumn> sqlResultColumns, String mainKey,
                             boolean shared) {
        DataSource dataSource = getDataSource();
        if (useShareCenter && (dataSource instanceof MdlDataSource) && shared) {
            boolean hasSharedConn = MdlConnection.hasSharedConnection();
            if (!hasSharedConn) {
                MdlDataSource mdlDataSource = (MdlDataSource) dataSource;
                mdlDataSource.openSharedConnection();
            }

            List<T> results = executeShareCenterRelationEntityQuery(sqlBuilder,
                    sqlResultColumns, mainKey);

            if (!hasSharedConn) {
                MdlConnection.closeSharedConnection();
            }

            return results;
        } else {
            return executeRelationEntityQuery(sqlBuilder, sqlResultColumns,
                    mainKey);
        }
    }

    protected <T> List<T> executeRelationEntityQuery(SqlBuilder sqlBuilder,
                                                     List<SqlResultColumn> resultColumns, String mainKey) {

        MdlLogInfo log = new MdlLogInfo(sqlBuilder);
        String sql = sqlBuilder.getSql();
        List<Object> parameters = sqlBuilder.getParameterList();
        JdbcTemplate jdbcTemplate = getJdbcTemplate();

        MdlEntityRelRowMapper<T> rowMapper = new MdlEntityRelRowMapper<T>(
                mainKey, configure, resultColumns);

        jdbcTemplate.query(sql, getPreparedStatementSetter(parameters), rowMapper);

        log.countCostTime();
        MdlLogger.record(log);

        return rowMapper.getEntityResults();
    }

    protected <T> List<T> executeShareCenterRelationEntityQuery(
            SqlBuilder sqlBuilder, List<SqlResultColumn> resultColumns,
            String mainKey) {

        MdlEntityRelRowMapper<T> rowMapper = new MdlEntityRelRowMapper<T>(
                mainKey, configure, resultColumns);

        executeShareCenterQuery(sqlBuilder, rowMapper);

        return rowMapper.getEntityResults();
    }

    /**
     * 通过该方法，主要查询返回结果为List<List<Object>>类型。
     *
     * @param sqlBuilder    查询语句
     * @param resultColumns 查询记过的列元数据信息。
     * @param encap         返回结果是否封装。
     * @param shared        是否需要使用共享中心查询。
     * @return 查询结果。
     */
    public List<List<Object>> query(SqlBuilder sqlBuilder,
                                    List<SqlResultColumn> resultColumns, boolean encap, boolean shared) {
        DataSource dataSource = getDataSource();
        if (useShareCenter && (dataSource instanceof MdlDataSource) && shared) {
            boolean hasSharedConn = MdlConnection.hasSharedConnection();
            if (!hasSharedConn) {
                MdlDataSource mdlDataSource = (MdlDataSource) dataSource;
                mdlDataSource.openSharedConnection();
            }

            List<List<Object>> results = executeShareCenterListQuery(
                    sqlBuilder, resultColumns, encap);

            if (!hasSharedConn) {
                MdlConnection.closeSharedConnection();
            }

            return results;
        } else {
            return executeListQuery(sqlBuilder, resultColumns, encap);
        }
    }

    private List<List<Object>> executeListQuery(SqlBuilder sqlBuilder,
                                                List<SqlResultColumn> resultColumns, boolean encap) {

        MdlLogInfo log = new MdlLogInfo(sqlBuilder);
        String sql = sqlBuilder.getSql();
        List<Object> parameters = sqlBuilder.getParameterList();
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        RowMapper rowMapper;
        if (encap) {
            rowMapper = new MdlMutilEntityEncopRowMapper(
                    configure, resultColumns);
        } else {
            rowMapper = new MdlUnencopRowMapper(configure,
                    resultColumns);
        }
        List<List<Object>> results = jdbcTemplate.query(sql, getPreparedStatementSetter(parameters), rowMapper);

        log.countCostTime();
        MdlLogger.record(log);

        return results;

    }

    private List<List<Object>> executeShareCenterListQuery(
            SqlBuilder sqlBuilder, List<SqlResultColumn> resultColumns,
            boolean encap) {

        List<List<Object>> results = null;
        if (encap) {
            MdlMutilEntityEncopRowMapper rowMapper = new MdlMutilEntityEncopRowMapper(
                    configure, resultColumns);
            results = executeShareCenterQuery(sqlBuilder, rowMapper);
        } else {
            MdlUnencopRowMapper rowMapper = new MdlUnencopRowMapper(configure,
                    resultColumns);
            results = executeShareCenterQuery(sqlBuilder, rowMapper);
        }

        return results;
    }

    /**
     * 该方法会把每一行的结果封装到Map中。Map的key为实体的别名或者实体的类名。
     *
     * @param sqlBuilder    查询语句
     * @param resultColumns 查询记过的列元数据信息。
     * @param shared        表明是否需要走共享中心查询。
     * @return 查询结果。
     */
    public List<Map<String, Object>> query(SqlBuilder sqlBuilder,
                                           List<SqlResultColumn> resultColumns, boolean shared) {
        DataSource dataSource = getDataSource();
        if (useShareCenter && (dataSource instanceof MdlDataSource) && shared) {
            boolean hasSharedConn = MdlConnection.hasSharedConnection();
            if (!hasSharedConn) {
                MdlDataSource mdlDataSource = (MdlDataSource) dataSource;
                mdlDataSource.openSharedConnection();
            }

            List<Map<String, Object>> results = executeShareCenterMapQuery(
                    sqlBuilder, resultColumns);

            if (!hasSharedConn) {
                MdlConnection.closeSharedConnection();
            }

            return results;
        } else {
            return executeMapQuery(sqlBuilder, resultColumns);
        }
    }

    private List<Map<String, Object>> executeMapQuery(SqlBuilder sqlBuilder,
                                                      List<SqlResultColumn> sqlResultColumns) {

        MdlLogInfo log = new MdlLogInfo(sqlBuilder);
        String sql = sqlBuilder.getSql();
        List<Object> parameters = sqlBuilder.getParameterList();
        JdbcTemplate jdbcTemplate = getJdbcTemplate();

        MdlMapEntityRowMapper rowMapper = new MdlMapEntityRowMapper(configure,
                sqlResultColumns);

        List<Map<String, Object>> results = jdbcTemplate.query(sql, getPreparedStatementSetter(parameters), rowMapper);

        log.countCostTime();
        MdlLogger.record(log);

        return results;
    }

    private List<Map<String, Object>> executeShareCenterMapQuery(
            SqlBuilder sqlBuilder, List<SqlResultColumn> sqlResultColumns) {

        MdlMapEntityRowMapper rowMapper = new MdlMapEntityRowMapper(configure,
                sqlResultColumns);
        List<Map<String, Object>> results = executeShareCenterQuery(sqlBuilder,
                rowMapper);

        return results;
    }

    /**
     * 该方法返回单实体结果的查询。
     *
     * @param sqlBuilder    查询语句
     * @param resultColumns 查询记过的列元数据信息。
     * @param shared        表明是否需要走共享中心查询。
     * @param entityClass   实体的Class
     * @return 查询结果
     */
    public <T> List<T> query(SqlBuilder sqlBuilder,
                             List<SqlResultColumn> resultColumns, Class<T> entityClass,
                             boolean shared) {
        DataSource dataSource = getDataSource();
        if (useShareCenter && (dataSource instanceof MdlDataSource) && shared) {
            boolean hasSharedConn = MdlConnection.hasSharedConnection();
            if (!hasSharedConn) {
                MdlDataSource mdlDataSource = (MdlDataSource) dataSource;
                mdlDataSource.openSharedConnection();
            }

            List<T> results = executeShareCenterEntityQuery(sqlBuilder,
                    resultColumns, entityClass);

            if (!hasSharedConn) {
                MdlConnection.closeSharedConnection();
            }

            return results;
        } else {
            return executeEntityQuery(sqlBuilder, resultColumns, entityClass);
        }
    }

    private <T> List<T> executeEntityQuery(SqlBuilder sqlBuilder,
                                           List<SqlResultColumn> resultColumns, Class<T> entityClass) {

        MdlLogInfo log = new MdlLogInfo(sqlBuilder);
        String sql = sqlBuilder.getSql();
        List<Object> parameters = sqlBuilder.getParameterList();
        JdbcTemplate jdbcTemplate = getJdbcTemplate();

        MdlEntityEncopRowMapper<T> rowMapper = new MdlEntityEncopRowMapper<T>(
                entityClass, resultColumns, configure);

        List<T> results = jdbcTemplate.query(sql, getPreparedStatementSetter(parameters), rowMapper);

        log.countCostTime();
        MdlLogger.record(log);

        return results;
    }

    private <T> List<T> executeShareCenterEntityQuery(SqlBuilder sqlBuilder,
                                                      List<SqlResultColumn> resultColumns, Class<T> entityClass) {

        MdlEntityEncopRowMapper<T> rowMapper = new MdlEntityEncopRowMapper<T>(
                entityClass, resultColumns, configure);
        List<T> results = executeShareCenterQuery(sqlBuilder, rowMapper);

        return results;
    }

    /**
     * 共享中心查询的基础方法，通过该方法执行具体的查询操作。
     *
     * @param sqlBuilder 查询语句及相关参数。
     * @param rowMapper  结果封装类
     * @return 经过封装后的结果。
     */
    private <T> List<T> executeShareCenterQuery(SqlBuilder sqlBuilder,
                                                RowMapper<T> rowMapper) {

        MdlLogInfo log = new MdlLogInfo(sqlBuilder, true);
        String sql = sqlBuilder.getSql();
        List<Object> parameters = sqlBuilder.getParameterList();
        List<T> results = new ArrayList<T>();

        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            Connection sharedConnection = MdlConnection.getSharedConnection();
            ps = sharedConnection.prepareStatement(sql);
            MdlUtil.fillParameters(ps, parameters);

            rs = ps.executeQuery();
            while (rs.next()) {
                T mapRow = rowMapper.mapRow(rs, 0);
                results.add(mapRow);
            }
        } catch (Exception e) {
            LOG.error("Exception happend when do share center query!", e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
                LOG.error("Exception happend when do share center query!", ex);
            }
        }

        log.countCostTime();
        MdlLogger.record(log);

        return results;
    }

    /**
     * 该方法用语执行任意更新操作，如Insert，Update和Delete。<br>
     * 根据参数的不同，会选择是不是同步执行共享中心。
     *
     * @param sqls   需要执行的SQL
     * @param shared 标示是否
     * @return 受影响的行数
     */
    public int update(List<SqlBuilder> sqls, boolean shared) {

        int affectCount = executeUpdate(sqls, shared);
        DataSource dataSource = getDataSource();
        if (useShareCenter && (dataSource instanceof MdlDataSource) && shared) {
            boolean hasSharedConn = MdlConnection.hasSharedConnection();
            if (!hasSharedConn) {
                // 如果确实不需要share center，（当前库和共享中心库相同，则返回。）
                if (!MdlConnection.getNeedSharedCenter()) {
                    return affectCount;
                }

                MdlDataSource mdlDataSource = (MdlDataSource) getDataSource();
                boolean needSharedCenter = mdlDataSource.needSharedCenter();
                if (needSharedCenter) {
                    mdlDataSource.openSharedConnection();
                } else {
                    return affectCount;
                }
            }

            affectCount += executeShareCenterUpdate(sqls);

            if (!hasSharedConn) {
                MdlConnection.closeSharedConnection();
            }
        }

        return affectCount;
    }

    /**
     * 该方法用语执行任意更新操作，如Insert，Update和Delete。<br>
     * 根据参数的不同，会选择是不是同步执行共享中心。
     *
     * @param sql    需要执行的SQL
     * @param shared 标示是否
     * @return 受影响的行数
     */
    public int update(SqlBuilder sql, boolean shared) {

        List<SqlBuilder> sqlList = Arrays.asList(sql);
        return update(sqlList, shared);
    }

    /**
     * 该方法用于执行任意更新操作，如Insert，Update和Delete。<br>
     * 根据参数的不同，但是该方法不会影响共享中心。
     *
     * @param sqls 需要执行的SQL
     */
    private int executeUpdate(List<SqlBuilder> sqls, boolean shared) {

        int affectRows = 0;
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        for (SqlBuilder sqlBuilder : sqls) {
            MdlLogInfo log = new MdlLogInfo(sqlBuilder, shared);
            String sql = sqlBuilder.getSql();
            final List<Object> parameters = sqlBuilder.getParameterList();
            if (sqlBuilder.isBatch()) {
                int[] batchUpdate = jdbcTemplate.batchUpdate(sql,
                        getBatchPreparedStatementSetter(parameters));
                for (int i : batchUpdate) {
                    affectRows += i;
                }
            } else {
                affectRows += jdbcTemplate.update(sql,
                        getPreparedStatementSetter(parameters));
            }

            log.countCostTime();
            MdlLogger.record(log);
        }

        return affectRows;
    }

    /**
     * 该方法用于执行任意更新操作，如Insert，Update和Delete。<br>
     * 根据参数的不同，但是该只会影响共享中心。如果共享中心不存在或者共享中心与当前库相同，<br>
     * 则该方法执行没有任何效果。
     *
     * @param sqlBuilderList 需要执行的SQL
     */
    private int executeShareCenterUpdate(List<SqlBuilder> sqlBuilderList) {

        Connection sharedConnection = MdlConnection.getSharedConnection();
        int affectRows = 0;
        for (SqlBuilder sqlBuilder : sqlBuilderList) {
            String sql = sqlBuilder.getSql();
            final List<Object> parameters = sqlBuilder.getParameterList();
            PreparedStatement ps = null;
            try {
                ps = sharedConnection.prepareStatement(sql);
                if (sqlBuilder.isBatch()) {
                    for (Object object : parameters) {
                        MdlUtil.fillParameters(ps, object);
                        ps.addBatch();
                    }

                    int[] executeBatch = ps.executeBatch();
                    for (int i = 0; i < executeBatch.length; i++) {
                        affectRows += i;
                    }
                } else {
                    MdlUtil.fillParameters(ps, parameters);
                    affectRows += ps.executeUpdate();
                }
            } catch (SQLException se) {
                throw new MdlException(se);
            } finally {
                try {
                    if (ps != null) {
                        ps.close();
                    }
                } catch (SQLException e) {
                    throw new MdlException(e);
                }
            }
        }

        return affectRows;
    }

    private PreparedStatementSetter getPreparedStatementSetter(final List<Object> parameters) {
        return new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                MdlUtil.fillParameters(ps, parameters);
            }
        };
    }

    /**
     * JdbcTemplate的batch执行参数转化器。
     *
     * @param batchParameters 需要批量执行的参数。
     * @return JdbcTemplate需要的对象。
     */

    private BatchPreparedStatementSetter getBatchPreparedStatementSetter(
            final List<Object> batchParameters) {
        return new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i)
                    throws SQLException {
                Object parameters = batchParameters.get(i);
                MdlUtil.fillParameters(ps, parameters);
            }

            @Override
            public int getBatchSize() {
                return batchParameters == null ? 0 : batchParameters.size();
            }
        };
    }





    public Configure getConfigure() {
        return configure;
    }

    public void setConfigure(Configure configure) {
        this.configure = configure;
    }

    public String getLogLevel() {
        return MdlLogger.getLogLevel();
    }

    public void setLogLevel(String logLevel) {
        MdlLogger.setLogLevel(logLevel);
    }

    public boolean isUseShareCenter() {
        return useShareCenter;
    }

    public void setUseShareCenter(boolean useShareCenter) {
        this.useShareCenter = useShareCenter;
    }
}
