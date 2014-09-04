package cn.cocho.dborm.test.utils;

import cn.cocho.dborm.util.DbormDataBase;

import java.sql.Connection;

public class ConnectionManagerC3p0 extends DbormDataBase {

    /**
     * 获得数据库连接
     *
     * @return
     * @author FIRST WIT
     */
    public Connection getConnection() {


        return null;
    }

    @Override
    public <T> T beforeInsert(T entity) {
        return entity;
    }

    @Override
    public <T> T beforeUpdate(T entity) {
        return entity;
    }

    @Override
    public <T> T beforeDelete(T entity) {
        return entity;
    }

}
