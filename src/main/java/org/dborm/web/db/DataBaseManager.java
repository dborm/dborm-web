package org.dborm.web.db;

import org.dborm.core.framework.CacheDborm;
import org.dborm.core.utils.DbormDataBase;
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
 * Created by shk
 */
public class DataBaseManager extends DbormDataBase {

//    Logger logger = Logger.getLogger(DBLogger.class);
    DBLogger logger = new DBLogger();

    private DataSource dataSource;

    /**
     * 当前用户的ID(可以从session或当前用户的线程中取),此处仅仅为示例
     */
    public static String userId = "sdfs23fsfsdfklsdflds";


    /**
     * 获得数据库连接
     *
     * @return
     */
    public Connection getConnection() {
        try {
            return getDataSource().getConnection();
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
    }


    @Override
    public <T> T beforeInsert(T entity) {
        super.beforeInsert(entity);
        CacheDborm cache = CacheDborm.getCache();
        ReflectUtilsDborm reflectUtils = new ReflectUtilsDborm();
        Map<String, Field> fields = cache.getEntityAllFieldsCache(entity.getClass());
        if (userId != null) {
            Object createUserId = reflectUtils.getFieldValue(fields.get(DBConstants.CREATE_USER_ID), entity);
            if (createUserId == null) {
                reflectUtils.setFieldValue(fields.get(DBConstants.CREATE_USER_ID), entity, userId);
            }
            Object lastModifyUserId = reflectUtils.getFieldValue(fields.get(DBConstants.MODIFY_USER_ID), entity);
            if (lastModifyUserId == null) {
                reflectUtils.setFieldValue(fields.get(DBConstants.MODIFY_USER_ID), entity, userId);
            }
        }

        Date currentTime = new Date();
        Object createTime = reflectUtils.getFieldValue(fields.get(DBConstants.CREATE_TIME), entity);
        if (createTime == null) {
            reflectUtils.setFieldValue(fields.get(DBConstants.CREATE_TIME), entity, currentTime);
        }
        Object lastModifyTime = reflectUtils.getFieldValue(fields.get(DBConstants.MODIFY_TIME), entity);
        if (lastModifyTime == null) {
            reflectUtils.setFieldValue(fields.get(DBConstants.MODIFY_TIME), entity, currentTime);
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
        CacheDborm cache = CacheDborm.getCache();
        ReflectUtilsDborm reflectUtils = new ReflectUtilsDborm();
        Map<String, Field> fields = cache.getEntityAllFieldsCache(entity.getClass());
        if (userId != null) {
            Object lastModifyUserId = reflectUtils.getFieldValue(fields.get(DBConstants.MODIFY_USER_ID), entity);
            if (lastModifyUserId == null) {
                reflectUtils.setFieldValue(fields.get(DBConstants.MODIFY_USER_ID), entity, userId);
            }
        }
        Object lastModifyTime = reflectUtils.getFieldValue(fields.get(DBConstants.MODIFY_TIME), entity);
        if (lastModifyTime == null) {
            reflectUtils.setFieldValue(fields.get(DBConstants.MODIFY_TIME), entity, new Date());
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
        CacheDborm cache = CacheDborm.getCache();
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
        CacheDborm cache = CacheDborm.getCache();
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
