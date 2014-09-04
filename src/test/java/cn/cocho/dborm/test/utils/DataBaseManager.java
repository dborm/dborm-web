package cn.cocho.dborm.test.utils;

import cn.cocho.dborm.util.DbormDataBase;
import cn.cocho.dborm.util.LoggerUtils;

import java.sql.*;

public class DataBaseManager extends DbormDataBase {

    /**
     * 获得数据库连接
     *
     * @return
     * @author FIRST WIT
     */
    public Connection getConnection() {
        return createConnection();
    }


    /**
     * 获得数据库连接
     *
     * @return
     * @author FIRST WIT
     * @time 2013-5-6上午10:46:44
     */
    private Connection createConnection() {
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://127.0.0.1:3306/mdl-test?useUnicode=true&characterEncoding=utf8";
        String username = "shk";
        String password = "shk";

        Connection conn = null;
        try {
            Class.forName(driver);// 加载驱动程序
            conn = DriverManager.getConnection(url, username, password);// 连续数据库
        } catch (Exception e) {
            LoggerUtils.error(e);
        }

        return conn;
    }

    @Override
    public <T> T beforeInsert(T entity) {

        return super.beforeInsert(entity);
    }
}
