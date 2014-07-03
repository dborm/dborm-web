package com.keqiaokeji.dborm.util;

import java.sql.Connection;

/**
 * 数据库连接相关的信息管理接口
 *
 * @author KEQIAO KEJI
 * @time 2014年1月15日 @下午5:08:18
 */
public abstract class DbormDataBase {

    /**
     * 获得数据库连接
     *
     * @return 数据库连接
     * @author KEQIAO KEJI
     * @time 2013-5-6上午10:46:44
     */
    public abstract Connection getConnection();

    /**
     * 新曾对象操作之前
     *
     * @param entity 实体对象
     * @param <T>    对象类型
     * @return 处理之后的对象
     */
    public <T> T beforeInsert(T entity){
        return entity;
    }

    /**
     * 修改对象操作之前
     *
     * @param entity 实体对象
     * @param <T>    对象类型
     * @return 处理之后的对象
     */
    public <T> T beforeUpdate(T entity){
        return entity;
    }

    /**
     * 删除对象操作之前
     *
     * @param entity 实体对象
     * @param <T>    对象类型
     * @return 处理之后的对象
     */
    public <T> T beforeDelete(T entity){
        return entity;
    }

    /**
     * 关闭数据库链接
     *
     * @param conn 数据库链接
     */
    public void closeConn(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception ignored) {
            }
        }
    }


}
