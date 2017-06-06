package com.tbc.paas.mdl.impl;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.tbc.paas.mdl.impl.rowmapper.MdlUnencopRowMapper;
import com.tbc.paas.mdl.util.*;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

import redis.clients.jedis.JedisCommands;

import com.tbc.framework.util.ExecutionContext;
import com.tbc.paas.mdl.MdlBaseDataService;
import com.tbc.paas.mdl.attach.AbstractMqlAttach;
import com.tbc.paas.mdl.cfg.domain.EntityMaping;
import com.tbc.paas.mdl.domain.MdlException;
import com.tbc.paas.mdl.ds.MdlDataSource;
import com.tbc.paas.mdl.ds.MdlDataSourceService;
import com.tbc.paas.mdl.metadata.MdlMetadataServiceImpl;
import com.tbc.paas.mdl.mql.MdlParserFactory;
import com.tbc.paas.mql.analyzer.MqlAnalyzer;
import com.tbc.paas.mql.domain.SqlResultColumn;
import com.tbc.paas.mql.domain.SqlTable;
import com.tbc.paas.mql.metadata.MqlMetadataService;
import com.tbc.paas.mql.notify.MqlNotify;
import com.tbc.paas.mql.parser.MqlDeleteParser;
import com.tbc.paas.mql.parser.MqlInsertParser;
import com.tbc.paas.mql.parser.MqlParser;
import com.tbc.paas.mql.parser.MqlPkCallback;
import com.tbc.paas.mql.parser.MqlSelectParser;
import com.tbc.paas.mql.parser.MqlUpdateParser;
import com.tbc.paas.mql.util.MqlOperation;
import com.tbc.paas.mql.util.SqlBuilder;
import com.tbc.paas.mql.util.SqlConstants;

public class MdlBasicDataServiceImpl extends MdlJdbcTemplate implements
        MdlBaseDataService, MqlPkCallback, SqlConstants, MdlConstants {

    public static final int OPT_TIME_INTERVAL = 60000;
    /**
     * 日志
     */
    public static final Logger LOG = Logger
            .getLogger(MdlBasicDataServiceImpl.class);

    /**
     * jedis对象
     */

    protected String tableAppCode;
    protected JedisCommands mdmJedisCommands;
    protected MqlPkCallback mqlPkCallback;
    protected MqlMetadataService metadataService;

    public MdlBasicDataServiceImpl() {
        super();
    }

    public void init() {
        if (metadataService == null) {
            Assert.notNull(mdmJedisCommands,
                    "mdm jedis must be setted before call MdlBasicDataServiceImpl's init method!");
            metadataService = new MdlMetadataServiceImpl(configure,
                    mdmJedisCommands);
        }

        mqlPkCallback = this;
    }

    @Override
    public void insertEntity(String appCode, String corpCode, Object entity) {
        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");
        Assert.notNull(entity, "Insert entity  is null!");

        EntityMaping entityMapping = getEntityMaping(entity);
        insertEntity(appCode, corpCode, entity, entityMapping);
    }

    protected void insertEntity(String appCode, String corpCode, Object entity,
                                EntityMaping entityMapping) {
        // 获取短类名
        String entityShortClassName = entityMapping.getEntitySimpleClassName();
        // 开始拼写 insert into 插入语句
        MdlBuilder insertMqlBuilder = new MdlBuilder(INSERT);
        insertMqlBuilder.append(INTO).append(entityShortClassName)
                .append(LEFT_BRACKET);
        // 定义sql值拼写字符串
        MdlBuilder sqlValueBuilder = new MdlBuilder(LEFT_BRACKET);

        // 获取映射对象所有字段
        List<Field> fieldList = entityMapping.getFieldList();

        // 循环拼接插入字段和参数值
        for (Field field : fieldList) {
            String fieldName = field.getName();
            // 通过字段名称获取对应的数据库列名
            String columnName = entityMapping
                    .getColumnNameByFieldName(fieldName);
            // 如果为自动维护字段则执行下一个字段
            if (AbstractMqlAttach.isAutoMaintainColumn(columnName)) {
                continue;
            }

            // 如果插入的值为空 则执行下一个字段
            Object value = ReflectUtil.getFieldValue(field, entity);
            if (value == null) {
                continue;
            }

            // 加入字段名和“,”逗号
            insertMqlBuilder.append(fieldName).append(COMMA);
            // sql值字符串加入:字段名 并将值保存到集合
            sqlValueBuilder.append(SqlConstants.COLON).escapeAppend(fieldName)
                    .append(COMMA).addParameter(fieldName, value);
        }

        // 处理插入扩展列
        processInsertExtColumns(entity, entityMapping, insertMqlBuilder,
                sqlValueBuilder);
        sqlValueBuilder.replaceOrAddLastSlice(RIGHT_BRACKET, COMMA);
        insertMqlBuilder.replaceOrAddLastSlice(RIGHT_BRACKET, COMMA)
                .append(VALUES).append(sqlValueBuilder);

        executeUpdate(appCode, corpCode, insertMqlBuilder);
    }

    /**
     * 目前这个方法没有针对拓展字段做任何处理，即忽略了所有拓展字段的存储。如有必要，将来再加上。
     */
    @Override
    public void insertEntity(String appCode, String corpCode, List<?> entityList) {
        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");
        if (entityList == null || entityList.isEmpty()) {
            return;
        }

        Object sampleEntity = entityList.get(0);
        EntityMaping entityMapping = getEntityMaping(sampleEntity);
        String tableName = entityMapping.getTableName();
        SqlBuilder insertBuilder = new SqlBuilder(INSERT);

        insertBuilder.append(INTO, tableName, LEFT_BRACKET);
        SqlBuilder valueBuilder = new SqlBuilder(LEFT_BRACKET);

        List<Field> fieldList = entityMapping.getFieldList();
        List<Field> autoMaintainField = new ArrayList<Field>(5);
        Iterator<Field> iterator = fieldList.iterator();
        while (iterator.hasNext()) {
            Field field = iterator.next();
            String fieldName = field.getName();
            String columnName = entityMapping
                    .getColumnNameByFieldName(fieldName);
            if (shouldAutoMaintainColumn(columnName)) {
                iterator.remove();
                autoMaintainField.add(field);
            } else {
                insertBuilder.append(columnName, COMMA);
                valueBuilder.append(QUESTION, COMMA);
            }
        }

        for (Field field : autoMaintainField) {
            String fieldName = field.getName();
            String columnName = entityMapping
                    .getColumnNameByFieldName(fieldName);
            insertBuilder.append(columnName, COMMA);
            valueBuilder.append(QUESTION, COMMA);
        }

        valueBuilder.replaceOrAddLastSlice(RIGHT_BRACKET, COMMA);
        insertBuilder.replaceOrAddLastSlice(RIGHT_BRACKET, COMMA)
                .append(VALUES).append(valueBuilder);
        insertBuilder.setBatch(true);

        List<Object> autoMaintainValues = new ArrayList<Object>(
                autoMaintainField.size());
        for (Field autoField : autoMaintainField) {
            String fieldName = autoField.getName();
            String columnName = entityMapping
                    .getColumnNameByFieldName(fieldName);
            if (columnName.equalsIgnoreCase(CREATE_BY)
                    || columnName.equalsIgnoreCase(LAST_MODIFY_BY)) {
                autoMaintainValues.add(ExecutionContext.getUserId());
            } else if (columnName.equalsIgnoreCase(CREATE_TIME)
                    || columnName.equalsIgnoreCase(LAST_MODIFY_TIME)) {
                long currentTimeMillis = System.currentTimeMillis();
                Timestamp createTimestamp = new Timestamp(currentTimeMillis);
                autoMaintainValues.add(createTimestamp);
            } else if (columnName.equalsIgnoreCase(OPT_TIME)) {
                long currentTimeMillis = System.currentTimeMillis()
                        / OPT_TIME_INTERVAL;
                autoMaintainValues.add(currentTimeMillis);
            }
        }

        int valueSize = fieldList.size() + autoMaintainField.size();
        for (Object entity : entityList) {
            List<Object> values = new ArrayList<Object>(valueSize);
            for (Field field : fieldList) {
                Object fieldValue = ReflectUtil.getFieldValue(field, entity);
                values.add(fieldValue);
            }

            for (int i = 0; i < autoMaintainField.size(); i++) {
                Field field = autoMaintainField.get(i);
                Object fieldValue = ReflectUtil.getFieldValue(field, entity);
                if (fieldValue == null) {
                    fieldValue = autoMaintainValues.get(i);
                }
                values.add(fieldValue);
            }

            insertBuilder.addParameter(values);
        }

        boolean shared = entityMapping.isShared();
        update(insertBuilder, shared);
    }

    @Override
    public void updateEntity(String appCode, String corpCode, Object entity) {
        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");
        Assert.notNull(entity, "Update entity  is null!");

        EntityMaping entityMapping = getEntityMaping(entity);
        updateEntity(appCode, corpCode, entity, entityMapping);
    }

    @Override
    public void updateEntity(String appCode, String corpCode, List<?> entityList) {
        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");
        if (entityList == null || entityList.isEmpty()) {
            return;
        }

        for (Object entity : entityList) {
            updateEntity(appCode, corpCode, entity);
        }
    }

    @Override
    public int deleteEntityByIds(String appCode, String corpCode,
                                 List<String> entityIdList, Class<?> entityClass) {
        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");
        Assert.notNull(entityClass, "Entity class can't be null!");
        Assert.notEmpty(entityIdList, "Deleted entity list can't be empty!");

        int affectRow = 0;
        for (String entityId : entityIdList) {
            int count = deleteEntityById(appCode, corpCode, entityId,
                    entityClass);
            affectRow += count;
        }

        return affectRow;
    }

    /**
     * 保存或更新实体记录
     */
    @Override
    public String saveOrUpdateEntity(String appCode, String corpCode,
                                     Object entity) {
        // 验证参数和实体对象不能为空
        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");
        Assert.notNull(entity, "Saved entity can't be null!");

        // 通过实体对象获取对应的映射对象
        EntityMaping entityMapping = getEntityMaping(entity);
        Field primaryField = entityMapping.getPrimaryField();
        // 获取主键值
        Object fieldValue = ReflectUtil.getFieldValue(primaryField, entity);
        // 如果主键值为空则新增，否则更新记录
        if (fieldValue == null || fieldValue.toString().trim().isEmpty()) {
            // 获取一个UUID赋予主键
            fieldValue = UUIDGenerator.getUUID();
            // 将主键的值保存进对应的实体类的主键字段中
            ReflectUtil.setFieldValue(primaryField, entity, fieldValue);
            // 执行新增方法插入记录
            insertEntity(appCode, corpCode, entity, entityMapping);
        } else {
            // 执行更新方法修改记录
            updateEntity(appCode, corpCode, entity, entityMapping);
        }

        return fieldValue.toString();
    }

    @Override
    public List<String> saveOrUpdateEntity(String appCode, String corpCode,
                                           List<?> entityList) {
        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");

        List<String> pkList = new ArrayList<String>();
        if (entityList == null || entityList.isEmpty()) {
            return pkList;
        }

        Object sampleEntity = entityList.get(0);
        EntityMaping entityMapping = getEntityMaping(sampleEntity);
        Field primaryField = entityMapping.getPrimaryField();

        List<Object> insertEntity = new ArrayList<Object>();
        Iterator<?> iterator = entityList.iterator();
        while (iterator.hasNext()) {
            Object entity = iterator.next();
            Object fieldValue = ReflectUtil.getFieldValue(primaryField, entity);
            // 如果主键值为空则新增，否则更新记录
            if (fieldValue == null || fieldValue.toString().trim().isEmpty()) {
                fieldValue = UUIDGenerator.getUUID();
                ReflectUtil.setFieldValue(primaryField, entity, fieldValue);

                iterator.remove();
                insertEntity.add(entity);
            }

            pkList.add(fieldValue.toString());
        }

        insertEntity(appCode, corpCode, insertEntity);
        updateEntity(appCode, corpCode, entityList);

        return pkList;
    }

    @Override
    public int deleteEntityById(String appCode, String corpCode,
                                String entityId, Class<?> entityClass) {
        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");
        Assert.hasText(entityId, "Deleted entity id can't be empty!");
        Assert.notNull(entityClass, "Entity class can't be null!");

        String entityClassName = entityClass.getName();
        EntityMaping entityMapping = configure
                .getEntityMappingByClassName(entityClassName);
        if (entityMapping == null) {
            throw new MdlException(entityClassName + "isn't a entity!");
        }
        MdlBuilder deleteMql = getDeleteMql(entityId, entityMapping);
        return executeUpdate(appCode, corpCode, deleteMql);
    }

    @Override
    public <T> T getEntityById(String appCode, String corpCode,
                               String entityId, Class<T> entityClass) {
        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");
        Assert.hasText(entityId, "Queryed entity id can't be empty!");
        Assert.notNull(entityClass, "Entity class can't be null!");

        String entityClassName = entityClass.getName();
        EntityMaping entityMapping = configure
                .getEntityMappingByClassName(entityClassName);

        MdlBuilder oneEntitySelectSql = getOneEntityQueryMql(entityMapping,
                entityId);
        MqlParser mqlParser = MdlParserFactory.getParser(appCode, corpCode,
                oneEntitySelectSql, metadataService, configure);
        mqlParser.setMqlPkCallback(mqlPkCallback);
        MqlSelectParser selectParser = (MqlSelectParser) mqlParser;
        List<SqlBuilder> selectSqls = mqlParser.parse();
        List<SqlResultColumn> sqlResultColumns = selectParser
                .getResultColumnList();

        SqlBuilder sqlBuilder = selectSqls.get(0);
        boolean share = executeInShareCenter(mqlParser);
        List<T> results = query(sqlBuilder, sqlResultColumns, entityClass,
                share);
        if (results == null || results.size() == 0) {
            return null;
        }

        return results.get(0);
    }

    @Override
    public List<Map<String, Object>> getMutilEntity(String appCode,
                                                    String corpCode, MdlBuilder mdlBuilder) {
        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");
        Assert.notNull(mdlBuilder, "Mql query build is null!");

        MqlParser mqlParser = MdlParserFactory.getParser(appCode, corpCode,
                mdlBuilder, metadataService, configure);
        mqlParser.setMqlPkCallback(mqlPkCallback);
        MqlSelectParser selectParser = (MqlSelectParser) mqlParser;
        List<SqlBuilder> selectSqls = mqlParser.parse();
        List<SqlResultColumn> sqlResultColumns = selectParser
                .getResultColumnList();
        SqlBuilder sqlBuilder = selectSqls.get(0);

        boolean share = executeInShareCenter(mqlParser);
        return query(sqlBuilder, sqlResultColumns, share);

    }

    @Override
    public <T> List<T> getMutilEntityWithRelation(String appCode,
                                                  String corpCode, String mainKey, MdlBuilder mdlBuilder) {
        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");
        Assert.hasText(mainKey, "Main key is invalid!");
        Assert.notNull(mdlBuilder, "Mql query build is null!");

        MqlParser mqlParser = MdlParserFactory.getParser(appCode, corpCode,
                mdlBuilder, metadataService, configure);
        mqlParser.setMqlPkCallback(mqlPkCallback);
        MqlSelectParser selectParser = (MqlSelectParser) mqlParser;
        List<SqlBuilder> selectSqls = mqlParser.parse();
        List<SqlResultColumn> sqlResultColumns = selectParser
                .getResultColumnList();
        SqlBuilder sqlBuilder = selectSqls.get(0);
        boolean shared = executeInShareCenter(mqlParser);

        return query(sqlBuilder, sqlResultColumns, mainKey, shared);
    }

    @Override
    public <T> List<T> executeRowTypeQuery(String appCode, String corpCode,
                                           MdlBuilder mdlBuilder) {
        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");
        Assert.notNull(mdlBuilder, "Mql query build is null!");

        List<List<Object>> data = executeQuery(appCode, corpCode, mdlBuilder,
                false);
        List<T> results = new ArrayList<T>();
        if (data == null) {
            return results;
        }

        for (List<Object> rowResult : data) {
            if (rowResult == null || rowResult.size() == 0) {
                continue;
            }

            @SuppressWarnings("unchecked")
            T t = (T) rowResult.get(0);
            results.add(t);
        }

        return results;
    }

    @Override
    public <T> T executeRowTypeUniqueQuery(String appCode, String corpCode,
                                           MdlBuilder mdlBuilder) {
        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");
        Assert.notNull(mdlBuilder, "Mql query build is null!");

        List<List<Object>> queryResult = executeQuery(appCode, corpCode,
                mdlBuilder, false);
        if (queryResult == null || queryResult.size() == 0) {
            return null;
        }

        List<Object> rowResult = queryResult.get(0);
        if (rowResult == null || rowResult.size() == 0) {
            return null;
        }

        @SuppressWarnings("unchecked")
        T data = (T) rowResult.get(0);

        return data;
    }

    @Override
    public <T> T getUniqueEntity(String appCode, String corpCode,
                                 MdlBuilder mdlBuilder) {
        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");
        Assert.notNull(mdlBuilder, "Mql query build is null!");

        List<List<Object>> queryResults = executeQuery(appCode, corpCode,
                mdlBuilder, true);
        if (queryResults == null || queryResults.size() == 0) {
            return null;
        }

        List<Object> rewEntity = queryResults.get(0);
        if (rewEntity == null || rewEntity.size() == 0) {
            return null;
        }

        @SuppressWarnings("unchecked")
        T entity = (T) rewEntity.get(0);

        return entity;
    }

    @Override
    public <T> List<T> getEntityList(String appCode, String corpCode,
                                     MdlBuilder mdlBuilder) {
        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");
        Assert.notNull(mdlBuilder, "Mql query build is null!");

        List<List<Object>> queryResults = executeQuery(appCode, corpCode,
                mdlBuilder, true);

        List<T> results = new ArrayList<T>();
        for (List<Object> list : queryResults) {
            if (list == null || list.size() == 0) {
                continue;
            }

            @SuppressWarnings("unchecked")
            T entity = (T) list.get(0);
            results.add(entity);
        }

        return results;
    }

    @Override
    public List<List<Object>> executeQuery(String appCode, String corpCode,
                                           MdlBuilder mdlBuilder, boolean encap) {
        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");
        Assert.notNull(mdlBuilder, "Mql query build is null!");

        MqlParser mqlParser = MdlParserFactory.getParser(appCode, corpCode,
                mdlBuilder, metadataService, configure);
        mqlParser.setMqlPkCallback(mqlPkCallback);
        MqlSelectParser selectParser = (MqlSelectParser) mqlParser;
        List<SqlBuilder> selectSqls = mqlParser.parse();
        List<SqlResultColumn> sqlResultColumns = selectParser
                .getResultColumnList();
        SqlBuilder sqlBuilder = selectSqls.get(0);

        boolean shared = executeInShareCenter(mqlParser);
        return query(sqlBuilder, sqlResultColumns, encap, shared);
    }

    /**
     * 执行SQL语句
     */
    @Override
    public int executeUpdate(String appCode, String corpCode,
                             MdlBuilder mdlBuilder) {
        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");
        Assert.notNull(mdlBuilder, "Mql query build is null!");

        MqlParser mqlParser = MdlParserFactory.getParser(appCode, corpCode,
                mdlBuilder, metadataService, configure);

        mqlParser.setMqlPkCallback(mqlPkCallback);
        List<SqlBuilder> updateSqlList = mqlParser.parse();

        boolean shared = executeInShareCenter(mqlParser);
        int affectRows = update(updateSqlList, shared);

        MqlNotify mqlNotify = mqlParser.getMqlNotify();
        processMonitorColumns(mqlNotify);

        return affectRows;
    }

    protected MdlBuilder getOneEntityQueryMql(EntityMaping entityMapping,
                                              String entityId) {

        String entityShortClassName = entityMapping.getEntitySimpleClassName();
        Field primaryField = entityMapping.getPrimaryField();
        String primaryFieldName = primaryField.getName();

        MdlBuilder selectBuilder = new MdlBuilder(SELECT);
        selectBuilder.append(ASTERISK).append(FROM)
                .append(entityShortClassName).append(WHERE)
                .append(primaryFieldName).append(EQUAL).append(COLON)
                .escapeAppend(primaryFieldName)
                .addParameter(primaryFieldName, entityId);

        return selectBuilder;
    }

    protected void processMonitorColumns(MqlNotify mqlNotify) {
        String tableName = mqlNotify.getTableName();
        EntityMaping entityMaping = configure
                .getEntityMapingByTableName(tableName);
        if (!entityMaping.isMonitored()) {
            return;
        }

        List<Object> primaryKeyValueList = mqlNotify
                .getAffectedPrimaryKeyValueList();
        if (primaryKeyValueList == null || primaryKeyValueList.isEmpty()) {
            return;
        }

        MqlOperation mqlOpertation = mqlNotify.getMqlOpertation();
        if (mqlOpertation == MqlOperation.DELETE) {
            MdlContext.addMqlNotify(mqlNotify);
        }

        List<String> keys = new ArrayList<String>();
        Map<String, Object> affectedColumnMap = mqlNotify
                .getAffectedColumnMap();
        Set<String> keySet = affectedColumnMap.keySet();
        for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext(); ) {
            String columnName = iterator.next();
            if (!entityMaping.isColumnMonitored(columnName)) {
                keys.add(columnName);
            }
        }
        keySet.removeAll(keys);

        if (affectedColumnMap.isEmpty()) {
            return;
        }

        List<String> complexColumnUpdate = mqlNotify.getComplexColumnUpdate();
        if (complexColumnUpdate == null || complexColumnUpdate.isEmpty()) {
            MdlContext.addMqlNotify(mqlNotify);
            return;
        }
    }

    protected MdlBuilder getDeleteMql(String entityId,
                                      EntityMaping entityMapping) {
        Field primaryField = entityMapping.getPrimaryField();
        String pkName = primaryField.getName();

        String entityShortClassName = entityMapping.getEntitySimpleClassName();
        MdlBuilder deletteSqlBuilder = new MdlBuilder(DELETE);
        deletteSqlBuilder.append(FROM).append(entityShortClassName)
                .append(WHERE).append(pkName).append(EQUAL).append(COLON)
                .escapeAppend(pkName).addParameter(pkName, entityId);

        return deletteSqlBuilder;
    }

    /*
     * 标示如果该列缺失时候，时候早自动补全值。
     */
    private static boolean shouldAutoMaintainColumn(String columnName) {
        if (columnName == null) {
            return false;
        }

        if (columnName.equalsIgnoreCase(CREATE_TIME)
                || columnName.equalsIgnoreCase(CREATE_BY)
                || columnName.equalsIgnoreCase(LAST_MODIFY_TIME)
                || columnName.equalsIgnoreCase(LAST_MODIFY_BY)
                || columnName.equalsIgnoreCase(OPT_TIME)) {
            return true;
        }

        return false;
    }

    /**
     * 处理插入扩展字段
     *
     * @param entity           实体类
     * @param entityMapping    实体类与数据库表结构映射关系对象
     * @param sqlColumnBuilder 插入字段SQL
     * @param sqlValueBuilder  插入值SQL
     */
    protected void processInsertExtColumns(Object entity,
                                           EntityMaping entityMapping, MdlBuilder sqlColumnBuilder,
                                           MdlBuilder sqlValueBuilder) {

        // 通过实体类和实体类与数据表结构映射关系对象获取扩展集合
        Map<String, Object> dynamicColumns = getEntityExtMap(entity,
                entityMapping);
        if (dynamicColumns == null || dynamicColumns.size() == 0) {
            return;
        }

        Set<Map.Entry<String, Object>> extSet = dynamicColumns.entrySet();
        // 循环拼写扩展集合字段列和参数信息
        for (Map.Entry<String, Object> extColumn : extSet) {
            String columnName = extColumn.getKey();
            Object value = extColumn.getValue();

            sqlColumnBuilder.append(columnName).append(COMMA);
            sqlValueBuilder.append(SqlConstants.COLON).escapeAppend(columnName)
                    .append(COMMA).addParameter(columnName, value);
        }
    }

    protected void updateEntity(String appCode, String corpCode, Object entity,
                                EntityMaping entityMapping) {

        String entityShortClassName = entityMapping.getEntitySimpleClassName();
        MdlBuilder updateMqlBuilder = new MdlBuilder(UPDATE);
        updateMqlBuilder.append(entityShortClassName).append(SET);

        Field primaryField = entityMapping.getPrimaryField();
        List<Field> fieldList = entityMapping.getFieldList();
        for (Field field : fieldList) {
            if (primaryField.equals(field)) {
                continue;
            }

            String fieldName = field.getName();
            String columnName = entityMapping
                    .getColumnNameByFieldName(fieldName);
            if (AbstractMqlAttach.isAutoMaintainColumn(columnName)) {
                continue;
            }

            Object value = ReflectUtil.getFieldValue(field, entity);
            if (value == null) {
                continue;
            }

            updateMqlBuilder.append(fieldName).append(EQUAL)
                    .append(SqlConstants.COLON).escapeAppend(fieldName)
                    .append(COMMA).addParameter(fieldName, value);
        }

        processUpdateExtColumns(entity, entityMapping, updateMqlBuilder);
        String primaryFieldName = primaryField.getName();
        Object pkValue = ReflectUtil.getFieldValue(primaryField, entity);

        updateMqlBuilder.replaceOrAddLastSlice(WHERE, COMMA)
                .append(primaryFieldName).append(EQUAL)
                .append(SqlConstants.COLON).escapeAppend(primaryFieldName)
                .addParameter(primaryFieldName, pkValue);

        executeUpdate(appCode, corpCode, updateMqlBuilder);
    }

    protected void processUpdateExtColumns(Object entity,
                                           EntityMaping entityMapping, MdlBuilder sqlColumnBuilder) {
        Map<String, Object> dynamicColumns = getEntityExtMap(entity,
                entityMapping);
        if (dynamicColumns == null || dynamicColumns.size() == 0) {
            return;
        }

        Set<Map.Entry<String, Object>> extSet = dynamicColumns.entrySet();
        for (Map.Entry<String, Object> extColumn : extSet) {
            String columnName = extColumn.getKey();
            Object value = extColumn.getValue();

            sqlColumnBuilder.append(columnName).append(EQUAL)
                    .append(SqlConstants.COLON).escapeAppend(columnName)
                    .append(COMMA).addParameter(columnName, value);
        }
    }

    /**
     * 通过实体对象获取配置的Bean的映射对象
     *
     * @param entity 实体记录对象
     * @return 配置过Bean的映射对象
     */
    protected EntityMaping getEntityMaping(Object entity) {
        Class<?> entityClass = entity.getClass();
        String entityClassName = entityClass.getName();
        // 通过实体类名获取映射对象
        EntityMaping entityMapping = configure
                .getEntityMappingByClassName(entityClassName);

        if (entityMapping == null) {
            // 如果映射为空，抛出异常 它不是mdl实体，请确认初始化元数据或redis
            String message = entityClassName
                    + " isn't a mdl entity,please make sure you have init it metadata  in configure file!";
            throw new MdlException(message);
        }

        return entityMapping;
    }

    /**
     * 获取实体扩展集合
     *
     * @param entity        实体类
     * @param entityMapping 实体类与数据库表结构映射关系对象
     * @return 扩展集合
     */
    protected Map<String, Object> getEntityExtMap(Object entity,
                                                  EntityMaping entityMapping) {
        // 获取动态扩展字段
        Field dynamicField = entityMapping.getDynamicField();
        // 通过字段和实体类获取字段值
        Object obj = ReflectUtil.getFieldValue(dynamicField, entity);
        if (obj == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        // 将值强转为map集合返回
                Map<String, Object> dynamicColumns = (Map<String, Object>) obj;

        return dynamicColumns;
    }

    @Override
    public List<List<Object>> executeGlobalQuery(String appCode,
                                                 String corpCode, MdlBuilder mdlBuilder, boolean encap) {

        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");
        Assert.notNull(mdlBuilder, "Mql query build is null!");

        MqlParser mqlParser = MdlParserFactory.getParser(appCode, corpCode,
                mdlBuilder, metadataService, configure);
        MqlSelectParser selectParser = (MqlSelectParser) mqlParser;
        List<SqlBuilder> selectSqls = mqlParser.parse();
        List<SqlResultColumn> sqlResultColumns = selectParser
                .getResultColumnList();
        SqlBuilder sqlBuilder = selectSqls.get(0);

        return query(sqlBuilder, sqlResultColumns, encap, true);
    }

    @Override
    public List<Map<String, Object>> executeGlobalQuery(String appCode,
                                                        String corpCode, MdlBuilder mdlBuilder) {

        Assert.hasText(appCode, "App code is invalid!");
        Assert.hasText(corpCode, "Corp code is invalid!");
        Assert.notNull(mdlBuilder, "Mql query build is null!");

        MqlParser mqlParser = MdlParserFactory.getParser(appCode, corpCode,
                mdlBuilder, metadataService, configure);
        MqlSelectParser selectParser = (MqlSelectParser) mqlParser;
        List<SqlBuilder> selectSqls = mqlParser.parse();
        List<SqlResultColumn> sqlResultColumns = selectParser
                .getResultColumnList();
        SqlBuilder sqlBuilder = selectSqls.get(0);

        return query(sqlBuilder, sqlResultColumns, true);
    }

    protected JdbcTemplate createJdbcTemplate(String appCode, String corpCode) {
        MdlDataSourceService dataSourceService = getDataSourceService();

        if (dataSourceService == null) {
            return getJdbcTemplate();
        }
        DataSource datasource = dataSourceService.getDataSource(appCode,
                corpCode);

        return new JdbcTemplate(datasource);
    }

    protected MdlDataSourceService getDataSourceService() {
        DataSource dataSource = getDataSource();
        if (!(dataSource instanceof MdlDataSource)) {
            return null;
        }

        MdlDataSource mdlDataSource = (MdlDataSource) dataSource;
        MdlDataSourceService dataSourceService = mdlDataSource
                .getDataSourceService();
        return dataSourceService;
    }

    public MqlMetadataService getMetadataService() {
        return metadataService;
    }

    public void setMetadataService(MqlMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    public JedisCommands getMdmJedisCommands() {
        return mdmJedisCommands;
    }

    public void setMdmJedisCommands(JedisCommands mdmJedisCommands) {
        this.mdmJedisCommands = mdmJedisCommands;
    }

    public void setOracle(boolean oracle) {
        MqlParser.setOracle(oracle);
    }

    public boolean isOracle() {
        return MqlParser.isOracle();
    }

    public boolean executeInShareCenter(MqlParser mqlParser) {
        MqlAnalyzer mqlAnalyzer = mqlParser.getMqlAnalyzer();
        String appCode = mqlAnalyzer.getAppCode().toLowerCase();
        List<SqlTable> sqlTableList = mqlAnalyzer.getSqlTableList();

        if (mqlParser instanceof MqlDeleteParser
                || mqlParser instanceof MqlInsertParser
                || mqlParser instanceof MqlUpdateParser) {
            SqlTable sqlTable = sqlTableList.get(0);
            String entityName = sqlTable.getTableName();
            EntityMaping mapping = configure
                    .getEntityMappingByClassName(entityName);

            return mapping.isShared();
        } else {
            String code = null;
            if (tableAppCode == null || tableAppCode.trim().isEmpty()) {
                code = "_" + appCode + "_";
            } else {
                code = "_" + tableAppCode + "_";
            }

            for (SqlTable table : sqlTableList) {
                String tableName = mqlAnalyzer.getRealTableName(table);
                int indexOf = tableName.toLowerCase().indexOf(code);
                if (indexOf == -1) {
                    return true;
                }
            }

            return false;
        }
    }

    public String getTableAppCode() {
        return tableAppCode;
    }

    public void setTableAppCode(String tableAppCode) {
        this.tableAppCode = tableAppCode;
    }

    @Override
    public List<Object> queryPrimaryKey(SqlBuilder sqlBuilder) {
        final String sql = sqlBuilder.getSql();
        final List<Object> parameterList = sqlBuilder.getParameterList();

        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        final List<Object> result = jdbcTemplate.query(sql, new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        MdlUtil.fillParameters(ps, parameterList);
                    }
                }, new RowMapper<Object>() {
                    @Override
                    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getObject(1);
                    }
                }
        );

        return result;
    }

    // @Override
    // public void batchCopyRelativeEntities(Class<?> entityClass,
    // List<String> entityIds, List<SqlTabRel> sqlTabRelList) {
    //
    // DataSource dataSource = getDataSource();
    // if (!(dataSource instanceof MdlDataSource)) {
    // return;
    // }
    //
    // String appCode = ExecutionContext.getAppCode();
    // String corpCode = ExecutionContext.getCorpCode();
    // SqlDataCopyUtil dataCopyUtil = new SqlDataCopyUtil(appCode, appCode,
    // DEFAULT_CORP_CODE, corpCode);
    // MdlDataSource mdlDataSource = (MdlDataSource) dataSource;
    // MdlDataSourceService dataSourceService = mdlDataSource
    // .getDataSourceService();
    // dataCopyUtil.setDataSourceService(dataSourceService);
    // dataCopyUtil.setConfigure(configure);
    //
    // dataCopyUtil.copy(entityClass, entityIds, sqlTabRelList);
    // }
}
