package org.dborm.web;

import org.dborm.core.api.DbormDataBase;
import org.dborm.core.framework.Cache;
import org.dborm.core.utils.ReflectUtilsDborm;
import org.dborm.core.utils.StringUtilsDborm;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

/**
 * 实现Dborm的数据源接口
 * Created by sky
 */
public class DataBaseManager extends DbormDataBase {

    private DataSource dataSource;

    /**
     * 当前用户的ID(可以从session或当前用户的线程中取),此处仅仅为示例
     */
    public static String userId = "sdfs23fsfsdfklsdflds";

    @Override
    public Object getConnection() {
        try {
            return getDataSource().getConnection();
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
    }

    @Override
    public void closeConnection(Object connection) {
        Connection conn = (Connection) connection;
        try {
            conn.close();
        } catch (SQLException e) {
            logger.error(e);
        }
    }


    @Override
    public <T> T beforeInsert(T entity) {
        super.beforeInsert(entity);
        Cache cache = Cache.getCache();
        ReflectUtilsDborm reflectUtils = new ReflectUtilsDborm();
        Map<String, Field> fields = cache.getEntityAllFieldsCache(entity.getClass());
        if (userId != null) {
            Object createUserId = reflectUtils.getFieldValue(fields.get(DBConstants.CREATE_BY), entity);
            if (createUserId == null) {
                reflectUtils.setFieldValue(fields.get(DBConstants.CREATE_BY), entity, userId);
            }
            Object lastModifyUserId = reflectUtils.getFieldValue(fields.get(DBConstants.UPDATE_BY), entity);
            if (lastModifyUserId == null) {
                reflectUtils.setFieldValue(fields.get(DBConstants.UPDATE_BY), entity, userId);
            }
        }

        Date currentTime = new Date();
        Object createTime = reflectUtils.getFieldValue(fields.get(DBConstants.CREATE_TIME), entity);
        if (createTime == null) {
            reflectUtils.setFieldValue(fields.get(DBConstants.CREATE_TIME), entity, currentTime);
        }
        Object lastModifyTime = reflectUtils.getFieldValue(fields.get(DBConstants.UPDATE_TIME), entity);
        if (lastModifyTime == null) {
            reflectUtils.setFieldValue(fields.get(DBConstants.UPDATE_TIME), entity, currentTime);
        }
        Object id = reflectUtils.getFieldValue(fields.get(DBConstants.ID), entity);
        if (id == null) {
            logger.debug("发现对象(" + entity.getClass().getName() + ")的主键id的值为空,将会用UUID自动填充该字段的值.");
            reflectUtils.setFieldValue(fields.get(DBConstants.ID), entity, new StringUtilsDborm().getUUID());
        }
        reflectUtils.setFieldValue(fields.get(DBConstants.DELETED), entity, DBConstants.DELETED_DEFAULT);
        return entity;
    }

    @Override
    public <T> T beforeUpdate(T entity) {
        super.beforeUpdate(entity);
        Cache cache = Cache.getCache();
        ReflectUtilsDborm reflectUtils = new ReflectUtilsDborm();
        Map<String, Field> fields = cache.getEntityAllFieldsCache(entity.getClass());
        if (userId != null) {
            Object lastModifyUserId = reflectUtils.getFieldValue(fields.get(DBConstants.UPDATE_BY), entity);
            if (lastModifyUserId == null) {
                reflectUtils.setFieldValue(fields.get(DBConstants.UPDATE_BY), entity, userId);
            }
        }
        Object lastModifyTime = reflectUtils.getFieldValue(fields.get(DBConstants.UPDATE_TIME), entity);
        if (lastModifyTime == null) {
            reflectUtils.setFieldValue(fields.get(DBConstants.UPDATE_TIME), entity, new Date());
        }
        return entity;
    }

    @Override
    public <T> T beforeReplace(T entity) {
        super.beforeReplace(entity);
        entity = beforeUpdate(entity);
        return entity;
    }

    @Override
    public <T> T beforeSaveOrReplace(T entity) {
        super.beforeSaveOrReplace(entity);
        ReflectUtilsDborm reflectUtils = new ReflectUtilsDborm();
        Cache cache = Cache.getCache();
        Map<String, Field> fields = cache.getEntityAllFieldsCache(entity.getClass());
        Object id = reflectUtils.getFieldValue(fields.get(DBConstants.ID), entity);
        if (id != null && new StringUtilsDborm().isBlank((String) id)) {
            reflectUtils.setFieldValue(fields.get(DBConstants.ID), entity, null);
        }
        return entity;
    }

    @Override
    public <T> T beforeSaveOrUpdate(T entity) {
        super.beforeSaveOrUpdate(entity);
        entity = beforeUpdate(entity);
        ReflectUtilsDborm reflectUtils = new ReflectUtilsDborm();
        Cache cache = Cache.getCache();
        Map<String, Field> fields = cache.getEntityAllFieldsCache(entity.getClass());
        Object id = reflectUtils.getFieldValue(fields.get(DBConstants.ID), entity);
        if (id != null && new StringUtilsDborm().isBlank((String) id)) {
            reflectUtils.setFieldValue(fields.get(DBConstants.ID), entity, null);
        }
        return entity;
    }


    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
